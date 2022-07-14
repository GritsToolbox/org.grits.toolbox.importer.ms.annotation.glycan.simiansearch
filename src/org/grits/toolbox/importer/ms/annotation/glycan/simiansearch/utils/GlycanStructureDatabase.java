package org.grits.toolbox.importer.ms.annotation.glycan.simiansearch.utils;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

public class GlycanStructureDatabase
{
    private String m_name = null;
    private String m_fileName = null;
    private String m_description = null;
    private String m_path = null;
    private Integer m_numberOfStructures = 0;

    @XmlAttribute(name = "name", required = true)
    public String getName()
    {
        return this.m_name;
    }

    public void setName(String a_name)
    {
        this.m_name = a_name;
    }

    @XmlAttribute(name = "description", required = false)
    public String getDescription()
    {
        return this.m_description;
    }

    public void setDescription(String a_description)
    {
        this.m_description = a_description;
    }

    @XmlAttribute(name = "structures", required = true)
    public Integer getNumberOfStructures()
    {
        return this.m_numberOfStructures;
    }

    public void setNumberOfStructures(Integer a_numberOfStructures)
    {
        this.m_numberOfStructures = a_numberOfStructures;
    }

    @XmlAttribute(name = "file", required = true)
    public String getFileName()
    {
        return this.m_fileName;
    }

    public void setFileName(String a_fileName)
    {
        this.m_fileName = a_fileName;
    }

    @XmlTransient
    public String getPath()
    {
        return this.m_path;
    }

    public void setPath(String a_path)
    {
        this.m_path = a_path;
    }

}
