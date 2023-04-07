package inc.flide.vim8.structures;

import java.util.List;

public class TrieNode {
    private int layer;
    private final TrieNode[] children = new TrieNode[FingerPosition.values().length];

    public TrieNode() {
        this.layer = -1;
    }

    public int getLayer() {
        return layer;
    }

    public void setLayer(int layer) {
        this.layer = layer;
    }

    public TrieNode[] getChildren() {
        return children;
    }

    public boolean isEmpty() {
        for (TrieNode child : children) {
            if (child != null) return false;
        }
        return true;
    }

    public int findLayer(List<FingerPosition> movementSequence) {
        TrieNode currentNode = this;

        for (FingerPosition position : movementSequence) {
            if (position == FingerPosition.INSIDE_CIRCLE) continue;
            if (currentNode.children[position.ordinal()] == null) return 0;
            currentNode = currentNode.children[position.ordinal()];
        }

        return currentNode.layer;
    }

    public void addMovementSequence(List<FingerPosition> movementSequence, int layer) {
        if (movementSequence == null || layer <= 0) return;
        TrieNode currentNode = this;
        for (FingerPosition position : movementSequence) {
            if (position == FingerPosition.INSIDE_CIRCLE) continue;
            if (currentNode.children[position.ordinal()] == null) {
                currentNode.children[position.ordinal()] = new TrieNode();
            }
            currentNode = currentNode.children[position.ordinal()];
        }
        currentNode.layer = layer;
    }

    public void copy(TrieNode other) {
        layer = other.layer;
        for (int i = 0; i < children.length; i++) {
            TrieNode child = other.children[i];
            if (child != null) {
                children[i] = new TrieNode();
                children[i].copy(child);
            }
        }

    }
}
