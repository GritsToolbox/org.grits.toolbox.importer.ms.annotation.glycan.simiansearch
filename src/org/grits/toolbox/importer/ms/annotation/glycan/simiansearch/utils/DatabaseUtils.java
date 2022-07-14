package org.grits.toolbox.importer.ms.annotation.glycan.simiansearch.utils;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.grits.toolbox.core.utilShare.ResourceLocatorUtils;

public class DatabaseUtils
{
    private static final String DATABASE_FOLDER = "/databases";
    private static final String INDEX_FILE = "databases.index";
    private static String DATABASE_PATH = null;

    public static GlycanStructureDatabaseIndex getGelatoDatabases() throws IOException, JAXBException
    {
        String t_path = DatabaseUtils.getDatabasePath();
        JAXBContext jaxbContext = JAXBContext.newInstance(GlycanStructureDatabaseIndex.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        GlycanStructureDatabaseIndex t_index = (GlycanStructureDatabaseIndex) jaxbUnmarshaller
                .unmarshal(new File(t_path + File.separator + INDEX_FILE));
        // now we need to add the path
        for (GlycanStructureDatabase t_database : t_index.getDatabase())
        {
            t_database.setPath(t_path + File.separator + t_database.getFileName());
        }
        return t_index;
    }

    public static String getDatabasePath() throws IOException
    {
        if (DatabaseUtils.DATABASE_PATH == null)
        {
            DatabaseUtils.DATABASE_PATH = ResourceLocatorUtils.getLegalPathOfResource(new DatabaseUtils(),
                    DATABASE_FOLDER);
        }
        return DatabaseUtils.DATABASE_PATH;
    }

}
