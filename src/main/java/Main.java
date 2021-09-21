import java.io.IOException;
import java.util.stream.Collectors;
import java.util.List;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Main {
    public static void main(final String[] args) throws IOException {
        String[] command = {"git", "rev-list", "--ancestry-path", "7f777ed95a19224294949e1b4ce56bbffcb1fe9f..dd104400dc551dd4098f35e12072e12c45822adc"};

        ProcessBuilder builder = new ProcessBuilder(command);
        builder.inheritIO().redirectOutput(ProcessBuilder.Redirect.PIPE);
        Process process = builder.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        List<String> commits = reader.lines().collect(Collectors.toList());

        System.out.println(biSearch(commits));
    }

    public static String biSearch(final List<String> list) {
        int l = 0;
        int r = list.size() - 1;
        while (l != r - 1) {
            int mid = (l + r) / 2;
            int exitCode = testCommit(list.get(mid));
            if (exitCode > 0) {
                r = mid;
            } else {
                l = mid;
            }
        }
        return list.get(r);
    }

    private static int testCommit(final String commit) {
        ProcessBuilder builder = new ProcessBuilder("git", "checkout", commit);
        int exitCode = -1;
        try {
            Process process = builder.start();
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            System.err.println("Something went wrong with checkout in testCommit: " + e.getMessage());
        }

        builder = new ProcessBuilder("python", "Lib/bisect.py");
        try {
            Process process = builder.start();
            process.waitFor();
            exitCode = process.exitValue();
        } catch (IOException | InterruptedException e) {
            System.err.println("Something went wrong with process in testCommit: " + e.getMessage());
        }
        return exitCode;
    }
}