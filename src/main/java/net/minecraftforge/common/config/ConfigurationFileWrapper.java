package net.minecraftforge.common.config;

import com.google.gson.JsonObject;
import net.minecraftforge.fml.common.FMLLog;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;

public class ConfigurationFileWrapper {
    public final FileType fileType;
    public final File configFile;
    public ConfigurationFileWrapper(File fileIn, FileType fileTypeIn){
        fileType = fileTypeIn;
        configFile = fileIn;
    }
    public ConfigurationFileWrapper(File fileIn){
        this(fileIn,FileType.CFG);
    }

    public File getConfigFile(){
        return configFile;
    }
    public void load(Configuration configuration){
        this.fileType.readFromFile(this, configuration);
    }
    public void save(Configuration configuration){
        this.fileType.writeToFile(configuration, this);
    }
    public enum FileType{
        CFG{
            @Override
            public void readFromFile(ConfigurationFileWrapper fileWrapper, Configuration configuration) {
                BufferedReader buffer = null;
                Configuration.UnicodeInputStreamReader input = null;
                File file = fileWrapper.getConfigFile();
                try
                {
                    if (file.getParentFile() != null)
                    {
                        file.getParentFile().mkdirs();
                    }

                    if (!file.exists())
                    {
                        // Either a previous load attempt failed or the file is new; clear maps
                        configuration.getCategories().clear();
                        configuration.getChildren().clear();
                        if (!file.createNewFile())
                            return;
                    }

                    if (file.canRead())
                    {
                        input = new Configuration.UnicodeInputStreamReader(new FileInputStream(file), configuration.defaultEncoding);
                        configuration.defaultEncoding = input.getEncoding();
                        buffer = new BufferedReader(input);

                        String line;
                        ConfigCategory currentCat = null;
                        Property.Type type = null;
                        ArrayList<String> tmpList = null;
                        int lineNum = 0;
                        String name = null;
                        configuration.setLoadedConfigVersion(null);

                        while (true)
                        {
                            lineNum++;
                            line = buffer.readLine();

                            if (line == null)
                            {
                                if (lineNum == 1)
                                    configuration.setLoadedConfigVersion(configuration.getDefinedConfigVersion());
                                break;
                            }

                            Matcher start = Configuration.CONFIG_START.matcher(line);
                            Matcher end = Configuration.CONFIG_END.matcher(line);

                            if (start.matches())
                            {
                                configuration.setFileName(start.group(1));
                                configuration.setCategories(new TreeMap<>());
                                continue;
                            }
                            else if (end.matches())
                            {
                                configuration.setFileName(end.group(1));
                                Configuration child = new Configuration();
                                configuration.setCategories(configuration.getCategories());
                                configuration.getChildren().put(configuration.getFileName(), child);
                                continue;
                            }

                            int nameStart = -1, nameEnd = -1;
                            boolean skip = false;
                            boolean quoted = false;
                            boolean isFirstNonWhitespaceCharOnLine = true;

                            for (int i = 0; i < line.length() && !skip; ++i)
                            {
                                if (Character.isLetterOrDigit(line.charAt(i)) || Configuration.ALLOWED_CHARS.indexOf(line.charAt(i)) != -1 || (quoted && line.charAt(i) != '"'))
                                {
                                    if (nameStart == -1)
                                    {
                                        nameStart = i;
                                    }

                                    nameEnd = i;
                                    isFirstNonWhitespaceCharOnLine = false;
                                }
                                else if (Character.isWhitespace(line.charAt(i)))
                                {
                                    // ignore space characters
                                }
                                else
                                {
                                    switch (line.charAt(i))
                                    {
                                        case '#':
                                            if (tmpList != null) // allow special characters as part of string lists
                                                break;
                                            skip = true;
                                            continue;

                                        case '"':
                                            if (tmpList != null) // allow special characters as part of string lists
                                                break;
                                            if (quoted)
                                            {
                                                quoted = false;
                                            }
                                            if (!quoted && nameStart == -1)
                                            {
                                                quoted = true;
                                            }
                                            break;

                                        case '{':
                                            if (tmpList != null) // allow special characters as part of string lists
                                                break;
                                            name = line.substring(nameStart, nameEnd + 1);
                                            if (!configuration.isCaseSensitiveCustomCategories())
                                                name = name.toLowerCase(Locale.ENGLISH);
                                            String qualifiedName = ConfigCategory.getQualifiedName(name, currentCat);

                                            ConfigCategory cat = configuration.getCategories().get(qualifiedName);
                                            if (cat == null)
                                            {
                                                currentCat = new ConfigCategory(name, currentCat);
                                                configuration.getCategories().put(qualifiedName, currentCat);
                                            }
                                            else
                                            {
                                                currentCat = cat;
                                            }
                                            name = null;

                                            break;

                                        case '}':
                                            if (tmpList != null) // allow special characters as part of string lists
                                                break;
                                            if (currentCat == null)
                                            {
                                                throw new RuntimeException(String.format("Config file corrupt, attempted to close to many categories '%s:%d'", configuration.getFileName(), lineNum));
                                            }
                                            currentCat = currentCat.parent;
                                            break;

                                        case '=':
                                            if (tmpList != null) // allow special characters as part of string lists
                                                break;
                                            name = line.substring(nameStart, nameEnd + 1);

                                            if (currentCat == null)
                                            {
                                                throw new RuntimeException(String.format("'%s' has no scope in '%s:%d'", name, configuration.getFileName(), lineNum));
                                            }

                                            Property prop = new Property(name, line.substring(i + 1), type, true);
                                            i = line.length();

                                            currentCat.put(name, prop);

                                            break;

                                        case ':':
                                            if (tmpList != null) // allow special characters as part of string lists
                                                break;
                                            type = Property.Type.tryParse(line.substring(nameStart, nameEnd + 1).charAt(0));
                                            nameStart = nameEnd = -1;
                                            break;

                                        case '<':
                                            if ((tmpList != null && i + 1 == line.length()) || (tmpList == null && i + 1 != line.length()))
                                            {
                                                throw new RuntimeException(String.format("Malformed list property \"%s:%d\"", configuration.getFileName(), lineNum));
                                            }
                                            else if (i + 1 == line.length())
                                            {
                                                name = line.substring(nameStart, nameEnd + 1);

                                                if (currentCat == null)
                                                {
                                                    throw new RuntimeException(String.format("'%s' has no scope in '%s:%d'", name, configuration.getFileName(), lineNum));
                                                }

                                                tmpList = new ArrayList<>();

                                                skip = true;
                                            }

                                            break;

                                        case '>':
                                            if (tmpList == null)
                                            {
                                                throw new RuntimeException(String.format("Malformed list property \"%s:%d\"", configuration.getFileName(), lineNum));
                                            }

                                            if (isFirstNonWhitespaceCharOnLine)
                                            {
                                                currentCat.put(name, new Property(name, tmpList.toArray(new String[tmpList.size()]), type));
                                                name = null;
                                                tmpList = null;
                                                type = null;
                                            } // else allow special characters as part of string lists
                                            break;

                                        case '~':
                                            if (tmpList != null) // allow special characters as part of string lists
                                                break;

                                            if (line.startsWith(Configuration.CONFIG_VERSION_MARKER))
                                            {
                                                int colon = line.indexOf(':');
                                                if (colon != -1)
                                                    configuration.setLoadedConfigVersion(line.substring(colon + 1).trim());

                                                skip = true;
                                            }
                                            break;

                                        default:
                                            if (tmpList != null) // allow special characters as part of string lists
                                                break;
                                            throw new RuntimeException(String.format("Unknown character '%s' in '%s:%d'", line.charAt(i), configuration.getFileName(), lineNum));
                                    }
                                    isFirstNonWhitespaceCharOnLine = false;
                                }
                            }

                            if (quoted)
                            {
                                throw new RuntimeException(String.format("Unmatched quote in '%s:%d'", configuration.getFileName(), lineNum));
                            }
                            else if (tmpList != null && !skip)
                            {
                                tmpList.add(line.trim());
                            }
                        }
                    }
                }
                catch (IOException e)
                {
                    FMLLog.log.error("Error while loading config {}.", configuration.getFileName(), e);
                }
                finally
                {
                    IOUtils.closeQuietly(buffer);
                    IOUtils.closeQuietly(input);
                }
            }

            @Override
            public void writeToFile(Configuration configuration, ConfigurationFileWrapper fileWrapper) {
                File file = fileWrapper.getConfigFile();
                try
                {
                    if (file.getParentFile() != null)
                    {
                        file.getParentFile().mkdirs();
                    }

                    if (!file.exists() && !file.createNewFile())
                    {
                        return;
                    }

                    if (file.canWrite())
                    {
                        FileOutputStream fos = new FileOutputStream(file);
                        BufferedWriter buffer = new BufferedWriter(new OutputStreamWriter(fos, configuration.defaultEncoding));

                        buffer.write("# Configuration file" + Configuration.NEW_LINE + Configuration.NEW_LINE);

                        if (configuration.getDefinedConfigVersion() != null)
                            buffer.write(Configuration.CONFIG_VERSION_MARKER + ": " + configuration.getDefinedConfigVersion() + Configuration.NEW_LINE + Configuration.NEW_LINE);

                        if (configuration.getChildren().isEmpty())
                        {
                            save(configuration, buffer);
                        }
                        else
                        {
                            for (Map.Entry<String, Configuration> entry : configuration.getChildren().entrySet())
                            {
                                buffer.write("START: \"" + entry.getKey() + "\"" + Configuration.NEW_LINE);
                                save(entry.getValue(), buffer);
                                buffer.write("END: \"" + entry.getKey() + "\"" + Configuration.NEW_LINE + Configuration.NEW_LINE);
                            }
                        }

                        buffer.close();
                        fos.close();
                    }
                }
                catch (IOException e)
                {
                    FMLLog.log.error("Error while saving config {}.", configuration.getFileName(), e);
                }
            }
            private static void save(Configuration configuration, BufferedWriter out) throws IOException
            {
                for (ConfigCategory cat : configuration.getCategories().values())
                {
                    if (!cat.isChild())
                    {
                        cat.write(out, 0);
                        out.newLine();
                    }
                }
            }
        };
        //TODO:JSON?

        public abstract void readFromFile(ConfigurationFileWrapper fileWrapper, Configuration configuration);
        public abstract void writeToFile(Configuration configuration, ConfigurationFileWrapper fileWrapper);
    }
}
