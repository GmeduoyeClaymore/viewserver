/*
 * Copyright 2016 Claymore Minds Limited and Niche Solutions (UK) Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.viewserver.sourcegen;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by bemm on 19/03/2015.
 */
public class SourceGen extends Task {
    private static Map<String, String> defaultReplacements = new HashMap<>();
    private Pattern specPattern = Pattern.compile("//\\s*([^:\\s]*)\\s*:\\s*((([^=]+)=([^,;]*)[,;]?)*(([^=]+)=([^,;]*)))");
    private Set<FileSet> fileSets = new HashSet<>();
    private Charset charset = Charset.forName("utf8");

    static {
        defaultReplacements.put("import io.viewserver.core._KeyType_;", "");
    }

    public void addFileSet(FileSet fileSet) {
        fileSets.add(fileSet);
    }

    public void execute() throws BuildException {
        String separator = System.getProperty("file.separator");
        String sourcePath = String.format("%ssrc%smain%sjava%s", separator, separator, separator, separator);
        String generatedSourcePath = String.format("%starget%sgenerated-sources%sjava%s", separator, separator, separator, separator);

        for (FileSet fileSet : fileSets) {
            DirectoryScanner directoryScanner = fileSet.getDirectoryScanner(getProject());
            File baseDir = directoryScanner.getBasedir();
            for (String templateFileName : directoryScanner.getIncludedFiles()) {
                File templateFile = new File(baseDir, templateFileName);
                long templateLastModified = templateFile.lastModified();
                try {
                    log("Generating source from " + templateFile.getPath(), Project.MSG_VERBOSE);
                    FileReader templateFileReader = new FileReader(templateFile);
                    BufferedReader templateBufferedReader = new BufferedReader(templateFileReader);

                    String specLine = templateBufferedReader.readLine();

                    Matcher matcher = specPattern.matcher(specLine);
                    if (!matcher.matches()) {
                        log("Invalid spec line in file '" + templateFileName + "'", Project.MSG_WARN);
                        continue;
                    }
                    String outputDirectory = templateFile.getParent().replace(sourcePath, generatedSourcePath);
                    String outputFilenameSpec = matcher.group(1);
                    if (outputFilenameSpec.length() == 0) {
                        outputFilenameSpec = templateFile.getName();
                    }
                    String[] specItems = matcher.group(2).split(";");
                    String templateContents = null;
                    for (String specItem : specItems) {
                        log("Spec item: " + specItem, Project.MSG_VERBOSE);
                        String outputFilename = outputFilenameSpec;
                        String[] tokenPairs = specItem.split(",");
                        for (String tokenPair : tokenPairs) {
                            String[] parts = tokenPair.split("=");
                            outputFilename = outputFilename.replace(parts[0], parts[1]);
                        }

                        File targetFile = new File(outputDirectory, outputFilename);
                        if (targetFile.exists()) {
                            long targetFileLastModified = targetFile.lastModified();
                            if (targetFileLastModified > templateLastModified) {
                                log(targetFile.getPath() + " is newer, skipping", Project.MSG_VERBOSE);
                                continue;
                            }
                        }

                        if (templateContents == null) {
                            if (templateBufferedReader != null) {
                                templateBufferedReader.close();
                                templateBufferedReader = null;
                            }

                            templateContents = readFile(templateFile);
                        }

                        String contents = templateContents;
                        for (Map.Entry<String, String> defaultReplacement : defaultReplacements.entrySet()) {
                            contents = contents.replace(defaultReplacement.getKey(), defaultReplacement.getValue());
                        }
                        for (String tokenPair : tokenPairs) {
                            String[] parts = tokenPair.split("=");
                            contents = contents.replace(parts[0], parts[1]);
                        }
                        targetFile.getParentFile().mkdirs();
                        Files.write(targetFile.toPath(), contents.getBytes(charset), StandardOpenOption.WRITE,
                                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                        log("Wrote " + targetFile.getPath(), Project.MSG_VERBOSE);
                    }
                } catch (Throwable e) {
                    log("An error occurred - " + e.getMessage(), e, Project.MSG_ERR);
                }
            }
        }
    }

    private String readFile(File file) throws IOException {
        try (FileInputStream stream = new FileInputStream(file)) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream, Charset.forName("utf8")));
            StringBuilder builder = new StringBuilder();
            char[] buffer = new char[8192];
            int read;
            while ((read = reader.read(buffer, 0, buffer.length)) > 0) {
                builder.append(buffer, 0, read);
            }
            return builder.toString();
        }
    }
}
