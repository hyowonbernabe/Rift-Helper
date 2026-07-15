package model;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LCUAuth {
    public static String port;
    public static String token;

    private static final Pattern PORT_PATTERN = Pattern.compile("--app-port=([0-9]+)");
    private static final Pattern TOKEN_PATTERN = Pattern.compile("--remoting-auth-token=([\\w-]+)");

    // How to read LeagueClientUx.exe's command line, most-compatible first.
    // PowerShell's Get-CimInstance is Microsoft's official replacement for wmic and works on every
    // Windows that runs League (Win10+), including builds where wmic.exe was removed (Win11 24H2+).
    // wmic is kept as a fallback for locked-down machines where PowerShell is blocked but wmic still
    // exists (older builds). Where-Object uses only single quotes so the command passes to
    // ProcessBuilder as one arg with no double-quote escaping to get wrong.
    private static final String[][] COMMANDS = {
            {"powershell.exe", "-NoProfile", "-NonInteractive", "-WindowStyle", "Hidden", "-Command",
                    "Get-CimInstance Win32_Process | Where-Object { $_.Name -eq 'LeagueClientUx.exe' } | Select-Object -ExpandProperty CommandLine"},
            {"wmic", "PROCESS", "WHERE", "name='LeagueClientUx.exe'", "GET", "commandline"}
    };

    public static boolean getLCUAuth() {
        for (String[] command : COMMANDS) {
            String commandLine = runAndCapture(command);
            if (commandLine == null) {
                continue; // this method isn't available on this system (e.g. wmic removed) — try the next
            }

            Matcher portMatcher = PORT_PATTERN.matcher(commandLine);
            Matcher tokenMatcher = TOKEN_PATTERN.matcher(commandLine);
            if (portMatcher.find() && tokenMatcher.find()) {
                port = portMatcher.group(1);
                token = tokenMatcher.group(1);
                return true;
            }
        }

        System.out.println("Error retrieving LCU authentication.");
        return false;
    }

    private static String runAndCapture(String[] command) {
        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append('\n');
                }
            }
            process.waitFor();
            return output.toString();
        } catch (Exception e) {
            return null; // command not found or failed to launch — signal caller to try the next method
        }
    }
}
