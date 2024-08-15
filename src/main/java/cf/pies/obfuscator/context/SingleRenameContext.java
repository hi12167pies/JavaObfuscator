package cf.pies.obfuscator.context;

import java.util.HashMap;
import java.util.Map;

public class SingleRenameContext {
    private final String prefix;
    private final Map<String, String> renames = new HashMap<>();
    private int methodCounter = 0;

    public SingleRenameContext(String prefix) {
        this.prefix = prefix;
    }

    public String create(String clazz) {
        String newName = prefix + methodCounter++;
        renames.put(clazz, newName);
        return newName;
    }

    public String get(String clazz) {
        return renames.getOrDefault(clazz, clazz);
    }
}
