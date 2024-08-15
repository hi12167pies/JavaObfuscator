package cf.pies.obfuscator;

import java.io.File;
import java.net.URL;

public class Main {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: <input> <output>");
            return;
        }

        try {
            File input = new File(args[0]);
            File output = new File(args[1]);

            Obfuscator obfuscator = new Obfuscator(input, output);

            // TODO: Make this an option
            obfuscator.setPaths(new URL[] {
                    new URL("file://C:/Program Files/Java/jre-1.8/lib/rt.jar")
            });

            obfuscator.obfuscate();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("[!] Something went wrong");
        }
    }
}
