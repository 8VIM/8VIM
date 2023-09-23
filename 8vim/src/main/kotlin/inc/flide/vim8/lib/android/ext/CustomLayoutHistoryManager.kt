package inc.flide.vim8.lib.android.ext

import android.content.Context
import android.net.Uri
import android.os.FileObserver
import android.util.Log
import arrow.core.Option
import inc.flide.vim8.lib.android.FileObserver
import inc.flide.vim8.lib.android.toFile
import inc.flide.vim8.models.appPreferenceModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class CustomLayoutHistoryManager internal constructor(context: Context) {
    companion object {
        private const val FILE_OBSERVER_MASK =
            FileObserver.MODIFY or
                FileObserver.DELETE or
                FileObserver.DELETE_SELF or
                FileObserver.MOVE_SELF or
                FileObserver.CLOSE_WRITE
        private var singleton: CustomLayoutHistoryManager? = null
        fun initialize(context: Context) {
            if (singleton == null) {
                singleton = CustomLayoutHistoryManager(context)
            }
        }

        @JvmStatic
        val instance: CustomLayoutHistoryManager
            get() = singleton!!
    }

    private val prefs by appPreferenceModel()
    private val ioScope = CoroutineScope(Dispatchers.IO)
    private val refreshGuard = Mutex()
    private val observers = hashMapOf<String, FileObserver>()
    private var fileChangeObserver: FileChangeObserver? = null

    init {
        val history = prefs.layout.custom.history
        val uris = LinkedHashSet(history.get())
        uris.toList().forEach { path ->
            Option
                .fromNullable(Uri.parse(path).toFile(context))
                .filter { it.exists() }
                .onSome { addObserver(path, context) }
                .onNone { uris.remove(path) }
        }
        history.set(uris)
        history.observe { newValue ->
            newValue.filter { !observers.containsKey(it) }.forEach { addObserver(it, context) }
        }
    }

    fun observe(fileChangeObserver: FileChangeObserver) {
        this.fileChangeObserver = fileChangeObserver
    }

    private fun addObserver(path: String, context: Context) {
        ioScope.launch {
            refreshGuard.withLock {
                if (!observers.containsKey(path)) {
                    val uri = Uri.parse(path)
                    uri.toFile(context)?.let {
                        observers[path] = FileObserver(it, FILE_OBSERVER_MASK) { event, _ ->
                            Log.d(
                                "FILEOBSERVER_EVENT",
                                "Path: $path Event with id ${event.toString(16)} happened"
                            )
                            when (event) {
                                FileObserver.CLOSE_WRITE -> handleModify(uri)
                                FileObserver.DELETE_SELF -> handleDelete(uri)
                            }
                        }
                        Log.d("FILEOBSERVER_EVENT", "Start watching: $path, ${it.exists()}")
                        observers[path]?.startWatching()
                    }
                }
            }
        }
    }

    private fun handleModify(uri: Uri) {
        val path = uri.toString()
        if (prefs.layout.current.get().path!!.toString() == path) {
            fileChangeObserver?.onChange(uri)?.let {
                if (it) {
                    removeObserver(path)
                }
            }
        }
    }

    private fun handleDelete(uri: Uri) {
        val path = uri.toString()
        if (prefs.layout.current.get().path!!.toString() == path) {
            removeObserver(path)
            fileChangeObserver?.onDelete(uri)
        }
    }

    private fun removeObserver(path: String) {
        observers[path]?.let {
            Log.d("FILEOBSERVER_EVENT", "Stop watching: $path")
            it.stopWatching()
            ioScope.launch {
                refreshGuard.withLock {
                    observers.remove(path)
                }
            }
        }
    }

    interface FileChangeObserver {
        fun onDelete(uri: Uri)
        fun onChange(uri: Uri): Boolean
    }
}
