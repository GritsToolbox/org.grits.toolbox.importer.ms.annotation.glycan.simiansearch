package org.grits.toolbox.importer.ms.annotation.glycan.simiansearch.utils;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "databaseIndex")
public class GlycanStructureDatabaseIndex
{
    private List<GlycanStructureDatabase> m_database = new ArrayList<GlycanStructureDatabase>();

    @XmlElement(required = false)
    public List<GlycanStructureDatabase> getDatabase()
    {
        return this.m_database;
    }

    public void setDatabase(List<GlycanStructureDatabase> a_database)
    {
        this.m_database = a_database;
    }

}
