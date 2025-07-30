package org.mariella.persistence.annotations_processing;

import jakarta.persistence.TableGenerator;
import org.mariella.persistence.mapping.TableGeneratorInfo;
import org.mariella.persistence.mapping.UniqueConstraintInfo;
import org.mariella.persistence.mapping.UnitInfo;

public class TableGeneratorInfoBuilder {

    private final TableGenerator tableGenerator;
    private final UnitInfo unitInfo;
    private final IModelToDb translator;

    public TableGeneratorInfoBuilder(TableGenerator tableGenerator, UnitInfo unitInfo, IModelToDb translator) {
        this.tableGenerator = tableGenerator;
        this.unitInfo = unitInfo;
        this.translator = translator;
    }

    public void buildInfo() {
        UniqueConstraintInfo[] uniqueConstraintInfos = new UniqueConstraintInfo[tableGenerator.uniqueConstraints().length];
        for (int i = 0; i < tableGenerator.uniqueConstraints().length; i++) {
            uniqueConstraintInfos[i] = new UniqueConstraintInfoBuilder(tableGenerator.uniqueConstraints()[i],
                    translator).buildUniqueConstraintInfo();
        }


        TableGeneratorInfo sqinfo = new TableGeneratorInfo();
        sqinfo.setAllocationSize(tableGenerator.allocationSize());
        sqinfo.setCatalog(tableGenerator.catalog());
        sqinfo.setInitialValue(tableGenerator.initialValue());
        sqinfo.setName(tableGenerator.name());
        sqinfo.setPkColumnName(tableGenerator.pkColumnName());
        sqinfo.setPkColumnValue(tableGenerator.pkColumnValue());
        sqinfo.setSchema(tableGenerator.schema());
        sqinfo.setTable(tableGenerator.table());
        sqinfo.setUniqueConstraintInfos(uniqueConstraintInfos);
        unitInfo.getTableGeneratorInfos().add(sqinfo);
    }


}
