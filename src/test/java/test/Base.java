package test;

import static org.junit.Assert.fail;

public class Base {

    protected void run() {
        // Get the branch name or test behavior from a system property or environment variable
        String branchBehavior = System.getProperty("branch.behavior", "fail");
        System.out.println(branchBehavior);// Default to "fail"

        switch (branchBehavior) {
            case "fail":
                fail("PR-1: This test always fails.");
                break;
            case "pass":
                // Do nothing (test will pass)
                break;
            case "random":
                int random = new java.util.Random().nextInt(2); // Randomly generates 0 or 1
                if (random == 0) {
                    fail("PR-3: This test randomly failed.");
                }
                break;
            default:
                fail("Invalid branch behavior: " + branchBehavior);
        }
    }
}