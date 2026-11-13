package trees;

import java.util.*;
import java.io.*;
import javax.swing.*;
import javax.swing.filechooser.*;

public class FamilyTree {

    private static class TreeNode<T> {
        private T data;
        private TreeNode<T> parent;
        private ArrayList<TreeNode<T>> children;

        TreeNode(T data) {
            this.data = data;
            children = new ArrayList<>();
        }

        T getData() {
            return data;
        }

        void addChild(TreeNode<T> childNode) {
            children.add(childNode);
            childNode.parent = this;
        }

        // Searches subtree at this node for a node
        // with the given data. Returns the node, or null if not found.
        TreeNode<T> getNodeWithData(T targetData) {
            // Does this node have the target data?
            if (Objects.equals(data, targetData))
                return this;
                    
            // No, recurse. Check all children of this node.
            for (TreeNode<T> child: children)
            {
                TreeNode<T> found = child.getNodeWithData(targetData);
                if (found != null)
                    return found;
            }
            
            // Not found anywhere.
            return null;
        }

        // Returns a list of ancestors of this TreeNode, starting with this node’s
        // parent and
        // ending with the root. Order is from recent to ancient.
        ArrayList<TreeNode<T>> collectAncestorsToList() {
            ArrayList<TreeNode<T>> ancestors = new ArrayList<>();

            TreeNode<T> node = this.parent;
            while (node != null) {
                ancestors.add(node);
                node = node.parent;
            }

            return ancestors;
        }

        public String toString() {
            return toStringWithIndent("");
        }

        private String toStringWithIndent(String indent) {
            String s = indent + data + "\n";
            indent += "  ";
            for (TreeNode<T> childNode : children)
                s += childNode.toStringWithIndent(indent);
            return s;
        }
    }

    private TreeNode<String> root;

    //
    // Displays a file browser so that user can select the family tree file.
    //
    public FamilyTree() throws IOException, TreeException {
        this(chooseTreeFile());
    }

    private static File chooseTreeFile() {
        // User chooses input file. This block doesn't need any work.
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Family tree text files", "txt");
        File dirf = new File("data");
        if (!dirf.exists()) dirf = new File(".");

        JFileChooser chooser = new JFileChooser(dirf);
        chooser.setFileFilter(filter);
        if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) System.exit(1);
        return chooser.getSelectedFile();
    }

    public FamilyTree(File treeFile) throws IOException, TreeException {
        try (BufferedReader br = new BufferedReader(new FileReader(treeFile))) {
            String line;
            while ((line = br.readLine()) != null)
                addLine(line);
        }
    }

    //
    // Line format is "parent:child1,child2 ..."
    // Throws TreeException if line is illegal.
    //
    private void addLine(String line) throws TreeException
    {
        if (line == null || line.trim().isEmpty())
            return;

        // Extract parent and array of children.
        int colonIndex = line.indexOf(':');
        if (colonIndex < 0)
            throw new TreeException("Invalid line format: " + line);

        String parent = line.substring(0, colonIndex).trim();
        String childrenString = line.substring(colonIndex + 1).trim();
        String[] childrenArray = childrenString.isEmpty() ? new String[0] : childrenString.split(",");

        // Find parent node. If root is null then the tree is empty and the
        // parent node must be constructed. Otherwise the parent node should be
        // somewhere in the tree.
        TreeNode<String> parentNode;
        if (root == null) {
            if (parent.isEmpty())
                throw new TreeException("Invalid root name: " + line);
            parentNode = root = new TreeNode<>(parent);
        } else {
            parentNode = root.getNodeWithData(parent);
            if (parentNode == null)
                throw new TreeException("Parent node not found: " + parent);
        }

        // Add child nodes to parentNode.
        // For each name in childrenArray, create a new node and add that node to parentNode.
        for (String childName : childrenArray) {
            String trimmedName = childName.trim();
            if (trimmedName.isEmpty())
                continue;

            TreeNode<String> childNode = new TreeNode<>(trimmedName);
            parentNode.addChild(childNode);
        }
    }

    // Returns the "deepest" node that is an ancestor of the node named name1, and
    // also is an
    // ancestor of the node named name2.
    //
    // "Depth" of a node is the "distance" between that node and the root. The depth
    // of the root is 0. The
    // depth of the root's immediate children is 1, and so on.
    //
    TreeNode<String> getMostRecentCommonAncestor(String name1, String name2) throws TreeException
    {
        if (root == null)
            throw new TreeException("Tree is empty.");

        // Get nodes for input names.
        TreeNode<String> node1 = root.getNodeWithData(name1);        // node whose data is name1
        if (node1 == null)
            throw new TreeException("Node not found: " + name1);
        TreeNode<String> node2 = root.getNodeWithData(name2);        // node whose data is name2
        if (node2 == null)
            throw new TreeException("Node not found: " + name2);

        // Get ancestors of node1 and node2.
        ArrayList<TreeNode<String>> ancestorsOf1 = node1.collectAncestorsToList();
        ArrayList<TreeNode<String>> ancestorsOf2 = node2.collectAncestorsToList();
        
        // Check members of ancestorsOf1 in order until you find a node that is also
        // an ancestor of 2.
        for (TreeNode<String> n1: ancestorsOf1)
            if (ancestorsOf2.contains(n1))
                return n1;

        // No common ancestor.
        return null;
    }

    public String toString() {
        return "Family Tree:\n\n" + root;
    }

    public static void main(String[] args) {
        try {
                FamilyTree tree = args.length == 0
                    ? new FamilyTree()
                    : new FamilyTree(new File(args[0]));
                System.out.println("Tree:\n" + tree + "\n**************\n");
        } catch (IOException x) {
            System.out.println("IO trouble: " + x.getMessage());
        } catch (TreeException x) {
            System.out.println("Input file trouble: " + x.getMessage());
        }
    }
}
