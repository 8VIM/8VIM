package inc.flide.vim8.geometry

class Dimension @JvmOverloads constructor(private var width: Int = 0, private var height: Int = 0) {
    fun getHeight(): Int {
        return height
    }

    fun setHeight(height: Int) {
        this.height = height
    }

    fun getWidth(): Int {
        return width
    }

    fun setWidth(width: Int) {
        this.width = width
    }
}