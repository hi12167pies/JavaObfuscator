package cf.pies.obfuscator.context;

import java.util.HashMap;
import java.util.Map;

public class MultiRenameContext {
    private final String prefix;
    private final Map<String, Map<String, String>> renames = new HashMap<>();
    private int counter = 0;

    public MultiRenameContext(String prefix) {
        this.prefix = prefix;
    }

    public String create(String clazz, String methodName) {
        Map<String, String> classRenames = this.renames.computeIfAbsent(clazz, key -> new HashMap<>());
        String newName = prefix + counter++;
        classRenames.put(methodName, newName);
        return newName;
    }

    public String get(String clazz, String methodName) {
        Map<String, String> renames = this.renames.computeIfAbsent(clazz, key -> new HashMap<>());
        return renames.getOrDefault(methodName, methodName);
    }
}
