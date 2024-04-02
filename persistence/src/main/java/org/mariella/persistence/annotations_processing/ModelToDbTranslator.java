package org.mariella.persistence.annotations_processing;

public class ModelToDbTranslator implements IModelToDb {

    public static final IModelToDb UPPERCASE = new ModelToDbTranslator(true);
    public static final IModelToDb LOWERCASE = new ModelToDbTranslator(false);

    private final boolean uppercase;

    private ModelToDbTranslator(boolean uppercase) {
        this.uppercase = uppercase;
    }

    @Override
    public String translate(String name) {
        return name == null ? null : (uppercase ? name.toUpperCase() : name.toLowerCase());
    }

}
