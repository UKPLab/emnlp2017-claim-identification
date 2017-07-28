/*
 * Copyright 2017
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.tudarmstadt.ukp.dkpro.discourse.pdtbparser;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FileUtils;
import org.jruby.Main;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Ivan Habernal
 */
public class PDTBParserWrapper
{

    private final Path tempDirectory;
    private final String parserRubyScript;

    public PDTBParserWrapper()
            throws IOException
    {
        tempDirectory = Files.createTempDirectory("temp_pdtb");

        //        FileUtils.copyFileToDirectory();
        File tempDir = tempDirectory.toFile();

        File tmpFile = File.createTempFile("tmp_pdtb", ".zip");

        InputStream stream = getClass().getClassLoader()
                .getResourceAsStream("pdtb-parser-v120415.zip");
        FileUtils.copyInputStreamToFile(stream, tmpFile);

        ZipFile zipFile;
        try {
            zipFile = new ZipFile(tmpFile);
            zipFile.extractAll(tempDir.getAbsolutePath());
        }
        catch (ZipException e) {
            throw new IOException(e);
        }

        String folderPrefix = "/pdtb-parser-v120415/src";
        String srcDir = tempDir.getCanonicalPath() + folderPrefix;

        // copy rewritten rb files
        copyFiles(new File(srcDir), "article.rb", "parser.rb");

        Files.walkFileTree(tempDirectory, new SimpleFileVisitor<Path>()
        {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                    throws IOException
            {
                Set<PosixFilePermission> permissions = new HashSet<>();
                permissions.add(PosixFilePermission.OWNER_EXECUTE);
                permissions.add(PosixFilePermission.GROUP_EXECUTE);
                permissions.add(PosixFilePermission.OTHERS_EXECUTE);
                permissions.add(PosixFilePermission.OWNER_READ);
                permissions.add(PosixFilePermission.GROUP_READ);
                permissions.add(PosixFilePermission.OTHERS_READ);

                Files.setPosixFilePermissions(file, permissions);

                return super.visitFile(file, attrs);
            }
        });

        parserRubyScript = srcDir + "/parser.rb";

        System.out.println(parserRubyScript);
    }

    public void run(File inFile, File tmpOutXml)
            throws IOException
    {
        // output tmp xml file
        System.out.println("In file: " + inFile.getAbsolutePath());
        System.out.println("Out file: " + tmpOutXml.getAbsolutePath());

        Main.main(new String[] { parserRubyScript, inFile.getAbsolutePath(),
                tmpOutXml.getAbsolutePath() });

        //        String s = FileUtils.readFileToString(tmpOutXml);
        //        System.out.println("Output:");
        //        System.out.println(s);

    }

    public void clean()
            throws IOException
    {
        FileUtils.deleteDirectory(tempDirectory.toFile());
    }

    private void copyFiles(File dir, String... files)
            throws IOException
    {
        for (String file : files) {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("ruby/" + file);
            FileUtils.copyInputStreamToFile(is, new File(dir, file));
        }
    }

    public static void main(String[] args)
            throws Exception
    {
        final PDTBParserWrapper pdtbParserWrapper = new PDTBParserWrapper();
        Path out = Files.createTempFile(pdtbParserWrapper.tempDirectory, "out", ".xml");
        pdtbParserWrapper.run(new File("src/main/resources/test302.txt"), out.toFile());
        pdtbParserWrapper.clean();
    }

}
