package org.mariella.persistence.mapping;

import java.io.PrintStream;
import java.util.Collections;
import java.util.List;

public abstract class RelationAttributeInfo extends AttributeInfo {
    private EntityInfo relatedEntityInfo;
    private RelationAttributeInfo reverseAttributeInfo;
    private List<JoinColumnInfo> joinColumnInfos = Collections.emptyList();

    @Override
    void initializeAdoptionCopy(AttributeInfo copy) {
        super.initializeAdoptionCopy(copy);

        ((RelationAttributeInfo) copy).relatedEntityInfo = relatedEntityInfo;
        ((RelationAttributeInfo) copy).joinColumnInfos = joinColumnInfos;

        // reverseAttributeInfo is built after adoption...
    }

    @Override
    public void overrideWith(AttributeInfo overriddenAttrInfo) {
        if (joinColumnInfos == null) {
            joinColumnInfos = ((RelationAttributeInfo) overriddenAttrInfo).joinColumnInfos;
        }
    }

    public EntityInfo getRelatedEntityInfo() {
        return relatedEntityInfo;
    }

    public void setRelatedEntityInfo(EntityInfo relatedEntityInfo) {
        this.relatedEntityInfo = relatedEntityInfo;
    }

    public RelationAttributeInfo getReverseAttributeInfo() {
        return reverseAttributeInfo;
    }

    public void setReverseAttributeInfo(RelationAttributeInfo reverseAttributeInfo) {
        this.reverseAttributeInfo = reverseAttributeInfo;
    }

    public List<JoinColumnInfo> getJoinColumnInfos() {
        return joinColumnInfos;
    }

    public void setJoinColumnInfos(List<JoinColumnInfo> joinColumnInfos) {
        this.joinColumnInfos = joinColumnInfos;
    }

    @Override
    void debugPrintAttributes(PrintStream out) {
        super.debugPrintAttributes(out);
        if (reverseAttributeInfo != null)
            out.print(" reverseAttributeInfo=" + reverseAttributeInfo.getName());
        if (joinColumnInfos != null) {
            for (JoinColumnInfo joinColInfo : joinColumnInfos) {
                joinColInfo.debugPrint(out);
            }
        }
    }

    @Override
    void debugPrintTypeInfo(PrintStream out) {
        out.print("<" + relatedEntityInfo.getName() + ">");
    }

}
