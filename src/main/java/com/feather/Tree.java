package com.feather;

class TreeEmptyException extends RuntimeException{

    public TreeEmptyException(String cause){
        super(cause);
    }
}

public class Tree<T> {

    private T elemnet;

    private Tree<T> firstChild;

    private Tree<T> nextSibling;

    private Tree<T> rootNode;

    public Tree(){}

    public Tree(T elemnet,Tree<T> firstChild,Tree<T> nextSibling){
        this.elemnet = elemnet;
        this.firstChild = firstChild;
        this.nextSibling = nextSibling;
    }

    public void CreateTreeRoot(T elemnet){
        rootNode = new Tree<>(elemnet,null,null);
    }

    public Tree FindNode(Tree<T> sourceNode,Tree<T> targetTree){
        if(rootNode == null){
            return null;
        }else if(rootNode.equals(sourceNode)){
            return rootNode;
        }else if(sourceNode.equals(targetTree)){
            return targetTree;
        }else{
            FindNode(sourceNode,rootNode.firstChild);
            FindNode(sourceNode,rootNode.firstChild.nextSibling);
        }
        return null;
    }

    public static <T> void PrintTree(Tree<T> startNode){
        System.out.println(startNode.elemnet+" "+startNode.firstChild+" "+startNode.nextSibling);
        for(Tree<T> tTree = startNode.firstChild; tTree != null; tTree = tTree.nextSibling){
            PrintTree(tTree);
        }
    }


    public boolean AddTreeNode(Tree<T> parentNode){
        if(rootNode == null){
            throw new TreeEmptyException("RootNode is empty");
        }
        if(parentNode.firstChild != null){
            Tree siblingTree = parentNode.firstChild;
            for(;siblingTree.nextSibling != null;siblingTree = siblingTree.nextSibling){}
            siblingTree.nextSibling = this;
        }else{
            parentNode.firstChild = this;
        }
        return true;
    }

    public static void main(String[] args){

    }

}
