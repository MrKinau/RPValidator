package dev.kinau.resourcepackvalidator.validator.models;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.kinau.resourcepackvalidator.ValidationJob;
import dev.kinau.resourcepackvalidator.utils.FileUtils;
import dev.kinau.resourcepackvalidator.validator.ValidationResult;
import dev.kinau.resourcepackvalidator.validator.context.FileContext;
import dev.kinau.resourcepackvalidator.validator.generic.FileContextValidator;

import java.util.HashMap;
import java.util.Map;

public class ModelTexturesExistsValidator extends FileContextValidator<JsonObject, Map<String, String>> {

    public ModelTexturesExistsValidator(Map<String, JsonObject> config) {
        super(config);
    }

    @Override
    protected ValidationResult<Map<String, String>> isValid(ValidationJob job, FileContext context, JsonObject textures) {
        Map<String, String> textureData = new HashMap<>();

        boolean failed = false;
        for (String key : textures.keySet()) {
            JsonElement value = textures.get(key);
            if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) {
                failed = true;
                failedError("Model has invalid texture registered (texture value is not string) for variable {} at {}", key, context.value().getPath());
            }
            textureData.put(key, value.getAsString());
        }
        if (failed)
            return failedSilent();

        for (Map.Entry<String, String> kv : textureData.entrySet()) {
            if (kv.getValue().startsWith("#")) {
                String referenceKey = kv.getValue().substring(1);
                if (textureData.containsKey(referenceKey)) continue;
            }
            if (!FileUtils.textureExists(context.namespace(), job.rootDir(), kv.getValue())) {
                failed = true;
                failedError("Model has linked texture that is not present ({}) at {}", kv.getKey() + " » " + kv.getValue(), context.value().getPath());
            }
        }
        if (failed)
            return failedSilent();

        return success(textureData);
    }


}
