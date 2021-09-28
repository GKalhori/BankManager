import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import static java.lang.Math.sqrt;

class Node { // nodes as banks
    boolean vertical;
    double x, y;
    Node left, right;
    Branch branch;

    Node(double x, double y, boolean v) {
        this.x = x;
        this.y = y;
        vertical = v;
        left = right = null;
    }
}

class Branch {
    HashMap<String, Point> info = new HashMap<>(); // branch name , coordinate
}

class Rectangle {
    private double x_min, y_min, x_max, y_max;

    Rectangle(double x_min, double y_min, double x_max, double y_max) {
        this.x_min = x_min;
        this.y_min = y_min;
        this.x_max = x_max;
        this.y_max = y_max;
    }

    boolean contains(Point p) {
        return (p.x() >= x_min) && (p.x() <= x_max) && (p.y() >= y_min) && (p.y() <= y_max);
    }

    public double x_min() {
        return x_min;
    }

    public double x_max() {
        return x_max;
    }

    public double y_min() {
        return y_min;
    }

    public double y_max() {
        return y_max;
    }
}

class Point {
    private double x, y;

    Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }
}

class QueueNode {
    Point value;
    QueueNode next;

    QueueNode(Point value) {
        this.value = value;
        this.next = null;
    }
}

class Queue {
    QueueNode front, rear;
    private int size;

    Queue() {
        this.front = this.rear = null;
        size = 0;
    }

    void enqueue(Point value) {
        size++;
        QueueNode temp = new QueueNode(value);
        if (this.rear == null) {
            this.front = this.rear = temp;
            return;
        }
        this.rear.next = temp;
        this.rear = temp;
    }

    void dequeue() {
        if (this.front == null)
            return;

        size--;
        this.front = this.front.next;
        if (this.front == null)
            this.rear = null;
    }

    public int size() {
        return size;
    }
}

class KD_Tree {
    private static Node root, newNode = null;
    private static Point point;
    private static Rectangle rect;
    private static int ctr = 0, order, max;
    private static double x, y, dist;
    private static String districtName, bankName, branchName, str;
    private static HashMap<String, Rectangle> district = new HashMap<>(); // district name -> Rect
    private static HashMap<String, String> nameOfTheCoordinate = new HashMap<>(); // coordinate -> name of the bank or branch
    private static HashMap<String, String> bankOfTheBranch = new HashMap<>(); // branch name -> bank name
    private static HashMap<String, Node> banks = new HashMap<>(); // name -> Node
    private static HashMap<String, String> types = new HashMap<>(); // coordinate -> type
    private static HashMap<String, Point> bank_main = new HashMap<>(); // list of the main banks
    private static HashMap<String, Point> bank_branch = new HashMap<>(); // list of the branches
    private static HashMap<String, Integer> fame_degree = new HashMap<>(); // most famous bank

    private Scanner input = new Scanner(System.in);

    KD_Tree() {
        root = null;
    }

    //////////////////////////////////////////////////////////////////////////////////// DELETE
    private static Node deleteNode(Node root, double x, double y, boolean vertical) {
        if (root == null)
            return null;

        if (root.x == x && root.y == y) {
            if (root.right != null) {
                Node min = findMin(root.right);
                root.x = min.x;
                root.y = min.y;
                root.right = deleteNode(root.right, min.x, min.y, !vertical);
            } else if (root.left != null) {
                Node min = findMin(root.left);
                root.x = min.x;
                root.y = min.y;
                root.right = deleteNode(root.left, min.x, min.y, !vertical);
            } else {  // If node is leaf
                root = null;
                return null;
            }
            return root;
        }

        if (root.vertical && x < root.x || !root.vertical && y < root.y)
            root.left = deleteNode(root.left, x, y, !vertical);
        else
            root.right = deleteNode(root.right, x, y, !vertical);
        return root;
    }

    private static void deleteNode(double x, double y) {
        deleteNode(root, x, y, true);
    }

    private static Node findMin(Node root, boolean d, boolean vertical) {
        if (root == null)
            return null;

        if (vertical == d) {
            if (root.left == null)
                return root;
            return findMin(root.left, d, !vertical);
        }
        return least(root, findMin(root.left, d, !vertical), findMin(root.right, d, !vertical), d);
    }

    private static Node findMin(Node root) {
        return findMin(root, true, true);
    }

    private static Node least(Node root1, Node root2, Node root3, boolean vertical) {
        Node res = root1;
        if (root2 != null && (vertical && root2.x < res.x || !vertical && root2.y < res.y))
            res = root2;
        if (root3 != null && (vertical && root3.x < res.x || !vertical && root3.y < res.y))
            res = root3;
        return res;
    }

    //////////////////////////////////////////////////////////////////////////////////// INSERT
    private static Node insert(Node root, double x, double y, boolean vertical) {
        if (root == null)
            return new Node(x, y, vertical);

        if (root.x == x && root.y == y) // don't allow to add duplicate node
            return root;
        if (root.vertical && x < root.x || !root.vertical && y < root.y)
            root.left = insert(root.left, x, y, !root.vertical);
        else
            root.right = insert(root.right, x, y, !root.vertical);

        return root;
    }

    private static void insert(double x, double y) {
        root = insert(root, x, y, true);
    }

    //////////////////////////////////////////////////////////////////////////////////// SEARCH
    private static boolean search(Node root, double x, double y) {
        if (root == null)
            return false;

        if (root.x == x && root.y == y)
            return true;
        if (root.vertical && x < root.x || !root.vertical && y < root.y)
            return search(root.left, x, y);
        else
            return search(root.right, x, y);
    }

    private static boolean search(double x, double y) {
        return search(root, x, y);
    }

    //////////////////////////////////////////////////////////////////////////////////// NEAREST NEIGHBOUR
    private static double distance(Point p1, Point p2) {
        return sqrt((p1.x() - p2.x()) * (p1.x() - p2.x()) + (p1.y() - p2.y()) * (p1.y() - p2.y()));
    }

    private static Point nearest_bank(Point point) {
        dist = Double.POSITIVE_INFINITY;
        if (search(point.x(), point.y()))
            return point;

        Point result = null;
        for (Map.Entry<String, Point> entry : bank_main.entrySet()) {
            Point value = entry.getValue();
            if (distance(value, point) < dist) {
                dist = distance(value, point);
                result = value;
            }
        }
        return result;
    }

    private static Point nearest_branch(Point point, String name) {
        dist = Double.POSITIVE_INFINITY;
        if (search(point.x(), point.y()))
            return point;

        Point result = null;
        for (Map.Entry<String, Point> entry : bank_branch.entrySet()) {
            Point value = entry.getValue();
            str = value.x() + "," + value.y();
            bankName = bankOfTheBranch.get(nameOfTheCoordinate.get(str));
            if (bankName.equals(name) && distance(value, point) < dist) {
                dist = distance(value, point);
                result = value;
            }
        }
        return result;
    }

    //////////////////////////////////////////////////////////////////////////////////// MENU
    private static void options() {
        System.out.println("1. Add Neighbourhood");
        System.out.println("2. Add Bank");
        System.out.println("3. Add Branch");
        System.out.println("4. Delete Branch");
        System.out.println("5. Banks of the Neighbourhood");
        System.out.println("6. Branches of the Bank");
        System.out.println("7. Nearest Bank");
        System.out.println("8. Nearest Branch");
        System.out.println("9. Available Banks");
        System.out.println("10. Bank with Most Branches");
        System.out.println("11. Most Famous Bank");
        System.out.println("12. Undo Time");
    }

    boolean notLeaf(Node node) {
        return root.right != null || root.left != null;
    }

    private void nextOrder() {
        System.out.println("What's Your Next Order?\n1. Back to Main Menu\n2. Exit");
        order = input.nextInt();
        while (order != 1 && order != 2) {
            System.out.println("Invalid Input, Enter a Valid Value:");
            order = input.nextInt();
        }
        if (order == 1)
            MainMenu();
        else
            System.exit(0);
    }

    //////////////////////////////////////////////////////////////////////////////////// RANGE
    Queue range(Node root, Rectangle rect, boolean vertical, Queue qq) {

        if (root == null) {
            return null;
        }
        point = new Point(root.x, root.y);
        if (rect.contains(point))
            qq.enqueue(point);

        if (notLeaf(root)) {
            if (!vertical) {
                if (rect.x_min() <= root.x && root.x <= rect.x_max()) {
                    if (root.right != null && root.left != null) { // has left & right child
                        range(root.right, rect, true, qq);
                        range(root.left, rect, true, qq);
                    } else if (root.left != null) { // has only left child
                        range(root.left, rect, true, qq);
                    } else if (root.right != null) { // has only right child
                        range(root.right, rect, true, qq);
                    }
                } else if (root.x >= rect.x_max()) {
                    if (root.left != null)
                        range(root.left, rect, true, qq);
                } else if (root.x <= rect.x_min()) {
                    if (root.right != null)
                        range(root.right, rect, true, qq);
                }
            } else {
                if (rect.y_min() <= root.y && root.y <= rect.y_max()) {
                    if (root.right != null && root.left != null) {
                        range(root.right, rect, false, qq);
                        range(root.left, rect, false, qq);
                    } else if (root.left != null) {
                        range(root.left, rect, false, qq);
                    } else if (root.right != null) {
                        range(root.right, rect, false, qq);
                    }
                } else if (root.y >= rect.y_max()) {
                    if (root.left != null)
                        range(root.left, rect, false, qq);
                } else if (root.y <= rect.y_min()) {
                    if (root.right != null)
                        range(root.right, rect, false, qq);
                }
            }
        }
        return qq;
    }

    Queue range(String districtName) {
        rect = district.get(districtName);
        Queue qq = new Queue();
        return range(root, rect, false, qq);
    }

    //////////////////////////////////////////////////////////////////////////////////// RADIUS_RANGE
    boolean inCircle(Point point, double radius, Point center) {
        double d = sqrt((point.x() - center.x()) * (point.x() - center.x()) + (point.y() - center.y()) * (point.y() - center.y()));
        return d <= radius;
    }

    private Queue radius_range(Node root, Point point, double radius, boolean vertical, Queue qq) {
        if (root == null)
            return null;

        Point current = new Point(root.x, root.y);

        if (inCircle(point, radius, current)) {
            Point p = new Point(root.x, root.y);
            qq.enqueue(p);
        }

        if (vertical) {
            if (current.x() > (point.x() - radius))
                radius_range(root.left, point, radius, false, qq);
            if (current.x() < (point.x() + radius))
                radius_range(root.right, point, radius, false, qq);
        } else {
            if (current.y() > (point.y() - radius))
                radius_range(root.left, point, radius, true, qq);
            if (current.y() < (point.y() + radius))
                radius_range(root.right, point, radius, true, qq);
        }
        return qq;
    }

    private Queue radius_range(Point point, double radius) {
        Queue qq = new Queue();
        return radius_range(root, point, radius, true, qq);
    }

    //////////////////////////////////////////////////////////////////////////////////// FUNCTIONS
    void addN(double x_min, double y_min, double x_max, double y_max, String districtName) {
        Rectangle rect = new Rectangle(x_min, y_min, x_max, y_max);
        district.put(districtName, rect);
    }

    void addB(double x, double y, String bankName) {
        insert(x, y);
        str = (x + "," + y);
        types.put(str, "Bank");
        nameOfTheCoordinate.put(str, bankName);
        newNode = new Node(x, y, true);
        banks.put(bankName, newNode);
        point = new Point(x, y);
        bank_main.put(bankName, point);
    }

    void addBr(double x, double y, String branchName) {
        Branch branch = new Branch();
        insert(x, y);
        point = new Point(x, y);
        branch.info.put(branchName, point);
        bank_branch.put(branchName, point);
        banks.get(bankName).branch = branch;
        str = (x + "," + y);
        types.put(str, "Branch");
        nameOfTheCoordinate.put(str, branchName);
        bankOfTheBranch.put(branchName, bankName);
    }

    void addBr2(double x, double y, String branchName, String bankName) {
        insert(x, y);
        point = new Point(x, y);
        if (banks.get(bankName).branch == null) {
            Branch branch = new Branch();
            branch.info.put(branchName, point);
            banks.get(bankName).branch = branch;
        } else {
            banks.get(bankName).branch.info.put(branchName, point);
        }
        str = (x + "," + y);
        types.put(str, "Branch");
        nameOfTheCoordinate.put(str, branchName);
        bankOfTheBranch.put(branchName, bankName);
        bank_branch.put(branchName, point);
    }

    void delBr(double x, double y) {
        point = new Point(x, y);
        str = x + "," + y;
        if (!search(x, y))
            System.out.println("Branch Not Found!");
        else if (types.get(str).equals("Bank"))
            System.out.println("This Coordinate Belongs to a Main Bank, You Can't Delete It.");
        else {
            deleteNode(x, y);
            types.remove(str);
            String br = nameOfTheCoordinate.get(str);
            String bn = bankOfTheBranch.get(br);
            banks.get(bn).branch.info.remove(br);
            bank_branch.remove(br);
            nameOfTheCoordinate.remove(br);
            bankOfTheBranch.remove(br);
            System.out.println("Branch Deleted Successfully!");
        }
    }

    void listB(String districtName) {
        Queue q = range(districtName);
        System.out.println("Banks & Branches of (" + districtName + ") Neighbourhood Are:");
        while (q.size() != 0) {
            str = q.front.value.x() + "," + q.front.value.y();
            q.dequeue();
            branchName = nameOfTheCoordinate.get(str);
            System.out.println("Name: " + branchName + " -> " + "(" + str + ") , Type = " + types.get(str));
            if (types.get(str).equals("branch")) {
                if (!fame_degree.containsKey(bankOfTheBranch.get(branchName)))
                    fame_degree.put(bankOfTheBranch.get(branchName), 1);
                else
                    fame_degree.put(bankOfTheBranch.get(branchName), fame_degree.get(bankOfTheBranch.get(branchName)) + 1);
            }
        }
    }

    void listBrs(String bankName) {
        if (banks.get(bankName).branch.info.size() == 0)
            System.out.println("Bank Doesn't Have Any Branch!");
        else {
            System.out.println("Branches of the (" + bankName + ") Bank Are:");
            banks.get(bankName).branch.info.forEach((key, value) ->
                    System.out.println(key + ": " + value.x() + "," + value.y()));
            if (!fame_degree.containsKey(bankName))
                fame_degree.put(bankName, 1);
            else
                fame_degree.put(bankName, fame_degree.get(bankName) + 1);
        }
    }

    void nearB(double x, double y) {
        point = new Point(x, y);
        point = nearest_bank(point);
        str = point.x() + "," + point.y();
        System.out.println("Coordinate of the Nearest Bank Is: (" + str + ")");
    }

    void nearBr(double x, double y, String bankName) {
        point = new Point(x, y);
        point = nearest_branch(point, bankName);
        str = point.x() + "," + point.y();
        System.out.println("Coordinate of the Nearest Branch Is: (" + str + ")");
        branchName = nameOfTheCoordinate.get(str);
        if (!fame_degree.containsKey(bankOfTheBranch.get(branchName)))
            fame_degree.put(bankOfTheBranch.get(branchName), 1);
        else
            fame_degree.put(bankOfTheBranch.get(branchName), fame_degree.get(bankOfTheBranch.get(branchName)) + 1);
    }

    void availB(double x, double y, double radius) {
        point = new Point(x, y);
        Queue q = radius_range(point, radius);
        System.out.println("Banks & Branches in the Given Radius:");
        while (q.size() != 0) {
            str = q.front.value.x() + "," + q.front.value.y();
            q.dequeue();
            branchName = nameOfTheCoordinate.get(str);
            System.out.println("Name: " + branchName + ", Coordinate: " + "(" + str + ") , Type: " + types.get(str));
            if (types.get(str).equals("branch")) {
                if (!fame_degree.containsKey(bankOfTheBranch.get(branchName)))
                    fame_degree.put(bankOfTheBranch.get(branchName), 1);
                else
                    fame_degree.put(bankOfTheBranch.get(branchName), fame_degree.get(bankOfTheBranch.get(branchName)) + 1);
            }
        }
    }

    void mostBrs() {
        max = 0;
        for (Map.Entry<String, Point> entry : bank_main.entrySet()) {
            bankName = entry.getKey();
            ctr = banks.get(bankName).branch.info.size();
            if (ctr > max) {
                max = ctr;
                str = bankName;
            }
        }
        System.out.println("Bank with Most Branches Is: " + str);
        if (!fame_degree.containsKey(str))
            fame_degree.put(str, 1);
        else
            fame_degree.put(str, fame_degree.get(str) + 1);
    }

    void fameB() {
        max = 0;
        for (Map.Entry<String, Integer> entry : fame_degree.entrySet()) {
            bankName = entry.getKey();
            ctr = entry.getValue();
            if (ctr > max) {
                max = ctr;
                str = bankName;
            }
        }
        System.out.println("Most Famous Bank Is: " + str + " -> " + bank_main.get(str).x() + "," + bank_main.get(str).y());
    }

    //////////////////////////////////////////////////////////////////////////////////// CASES
    void MainMenu() {
        System.out.println("Welcome to the Banks Management System");
        System.out.println("Choose What You Want to Do:");
        options();
        Scanner input = new Scanner(System.in);
        order = input.nextInt();
        while (order < 1 || order > 12) {
            System.out.println("Enter a Valid Number");
            order = input.nextInt();
        }
        switch (order) {
            case 1:
                System.out.println("Enter Name of the Neighbourhood:");
                districtName = input.next();
                System.out.println("x_min:");
                double x_min = input.nextDouble();
                System.out.println("x_max:");
                double x_max = input.nextDouble();
                System.out.println("y_min:");
                double y_min = input.nextDouble();
                System.out.println("y_max:");
                double y_max = input.nextDouble();
                addN(x_min, y_min, x_max, y_max, districtName);
                nextOrder();
                break;

            case 2:
                System.out.println("Enter Name of the Bank:");
                bankName = input.next();
                System.out.println("Enter Banks's Coordinate:");
                x = input.nextDouble();
                y = input.nextDouble();
                while (search(x, y)) {
                    System.out.println("This Coordinate Is Taken, Enter Another One:");
                    x = input.nextDouble();
                    y = input.nextDouble();
                }
                addB(x, y, bankName);
                System.out.println("Do You Want to Add Branch?\n1. Yes\n2. No");
                order = input.nextInt();
                if (order == 1) {
                    System.out.println("How Many Branches You Want to Add?");
                    order = input.nextInt();
                    for (int i = 0; i < order; i++) {
                        System.out.println("Enter Name of the Branch:");
                        branchName = input.next();
                        System.out.println("Enter Coordinate of the Branch:");
                        x = input.nextDouble();
                        y = input.nextDouble();
                        while (search(x, y)) {
                            System.out.println("This Coordinate Is Taken, Enter Another One");
                            x = input.nextDouble();
                            y = input.nextDouble();
                        }
                        addBr(x, y, branchName);
                    }
                }
                nextOrder();
                break;

            case 3:
                System.out.println("Enter Name of the Bank:");
                bankName = input.next();
                while (!bank_main.containsKey(bankName)) {
                    System.out.println("This Bank Doesn't Exist, Enter a Valid Value:");
                    bankName = input.next();
                }
                System.out.println("Enter Name of the Branch:");
                branchName = input.next();
                System.out.println("Enter Coordinate of the Branch:");
                x = input.nextDouble();
                y = input.nextDouble();
                while (search(x, y)) {
                    System.out.println("This Coordinate Is Taken, Enter Another One");
                    x = input.nextDouble();
                    y = input.nextDouble();
                }
                addBr2(x, y, branchName, bankName);
                nextOrder();
                break;

            case 4:
                System.out.println("Enter Coordinate of the Branch You Want to Delete");
                x = input.nextDouble();
                y = input.nextDouble();
                delBr(x, y);
                nextOrder();
                break;

            case 5:
                System.out.println("Enter Name of the Neighbourhood:");
                districtName = input.next();
                while (!district.containsKey(districtName)) {
                    System.out.println("This District Doesn't Exist, Enter a Valid Value:");
                    districtName = input.next();
                }
                listB(districtName);
                nextOrder();
                break;

            case 6:
                System.out.println("Enter Name of the Bank:");
                bankName = input.next();
                listBrs(bankName);
                nextOrder();
                break;

            case 7:
                System.out.println("Enter Coordinate of the Point:");
                x = input.nextDouble();
                y = input.nextDouble();
                nearB(x, y);
                nextOrder();
                break;

            case 8:
                System.out.println("Enter Name of the Bank:");
                bankName = input.next();
                System.out.println("Enter Coordinate of the Point:");
                x = input.nextDouble();
                y = input.nextDouble();
                nearBr(x, y, bankName);
                nextOrder();
                break;

            case 9:
                System.out.println("Enter Coordinate of the Point:");
                x = input.nextDouble();
                y = input.nextDouble();
                System.out.println("Enter the Radius:");
                double radius = input.nextDouble();
                availB(x, y, radius);
                nextOrder();
                break;

            case 10:
                mostBrs();
                nextOrder();
                break;

            case 11:
                fameB();
                nextOrder();
                break;

            case 12:
                System.out.println("Undo Isn't Possible :)");
                break;
        }
    }
}

//////////////////////////////////////////////////////////////////////////////////// TEST PROGRAM
class Test {
    public static void main(String[] args) {
        KD_Tree manager = new KD_Tree();
        manager.MainMenu();
    }
}