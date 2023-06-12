package dev.kinau.resourcepackvalidator.validator.generic;

import com.google.gson.JsonElement;
import dev.kinau.resourcepackvalidator.ValidationJob;
import dev.kinau.resourcepackvalidator.utils.FileUtils;
import dev.kinau.resourcepackvalidator.validator.ValidationResult;
import dev.kinau.resourcepackvalidator.validator.Validator;
import dev.kinau.resourcepackvalidator.validator.context.EmptyValidationContext;
import dev.kinau.resourcepackvalidator.validator.context.FileContext;
import dev.kinau.resourcepackvalidator.validator.context.FileContextWithData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class JsonElementMapper extends Validator<ValidationJob, EmptyValidationContext, Collection<FileContextWithData<JsonElement>>> {

    protected final List<Validator<JsonElement, FileContext, ?>> chainedValidators = new ArrayList<>();

    public <V> Validator<ValidationJob, EmptyValidationContext, Collection<FileContextWithData<JsonElement>>> thenForEachElement(Validator<JsonElement, FileContext, V> next) {
        this.chainedValidators.add(next);
        return this;
    }

    @Override
    public boolean validate(ValidationJob job, EmptyValidationContext context, ValidationJob data) {
        ValidationResult<Collection<FileContextWithData<JsonElement>>> result = isValid(job, context, data);

        for (Validator<JsonElement, FileContext, ?> chainedValidator : chainedValidators) {
            for (FileContextWithData<JsonElement> contextAndData : result.result()) {
                chainedValidator.validate(job, contextAndData, contextAndData.data());
            }
        }
        return true;
    }

    @Override
    protected ValidationResult<Collection<FileContextWithData<JsonElement>>> isValid(ValidationJob job, EmptyValidationContext context, ValidationJob data) {
        List<FileContextWithData<JsonElement>> filesWithData = new ArrayList<>();
        job.jsonCache().values().forEach(namespaceJsonCache -> {
            if (!namespaceJsonCache.cache().containsKey(FileUtils.Directory.MODELS)) return;
            namespaceJsonCache.cache().get(FileUtils.Directory.MODELS).forEach(element -> {
                filesWithData.add(new FileContextWithData<>(namespaceJsonCache.namespace(), element.file(), element.element()));
            });
        });
        return success(filesWithData);
    }
}