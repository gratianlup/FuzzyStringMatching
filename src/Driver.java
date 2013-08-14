// Copyright (c) 2013 Gratian Lup. All rights reserved.
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are
// met:
//
// * Redistributions of source code must retain the above copyright
// notice, this list of conditions and the following disclaimer.
//
// * Redistributions in binary form must reproduce the above
// copyright notice, this list of conditions and the following
// disclaimer in the documentation and/or other materials provided
// with the distribution.
//
// * The name "FuzzyStringMatching" must not be used to endorse or promote
// products derived from this software without prior written permission.
//
// * Products derived from this software may not be called "FuzzyStringMatching" nor
// may "FuzzyStringMatching" appear in their names without prior written
// permission of the author.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
// A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
// OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
// SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
// LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
// DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
// THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Driver {
    private static class ParsedArguments {
        public boolean valid;
        public boolean useCache;
        public boolean verbose;
        public String dictionaryFile;
        public String reversedDictionaryFile;
        public String testFile;
        public String graphvizFile;
        public int maxErrors;
    }

    private static void writeAutomatonToDOT(LevenshteinAutomaton automaton,
                                            String filePath) {
        try {
            FileWriter fileWriter = new FileWriter(filePath);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            automaton.exportToDOT(bufferedWriter);
            bufferedWriter.close();
            fileWriter.close();
        }
        catch(Exception ex) {
            System.out.println("Failed to write automaton to DOT file!");
        }
    }

    private static List<String> readWordList(String filePath) throws IOException {
        FileInputStream stream = null;
        List<String> words = new ArrayList<String>();

        try {
            stream = new FileInputStream(filePath);
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            String line = reader.readLine();

            while(line != null) {
                words.add(line.trim());
                line = reader.readLine();
            }
        }
        catch(IOException ex) {
            System.out.println("Failed to read trie file!");
            throw ex;
        }
        finally {
            if(stream != null) stream.close();
        }

        return words;
    }

    private static Trie buildTrie(List<String> words) {
        // Trie trie = new SimpleTrie();
        Trie trie = new CompactTrie();
        trie.addWords(words);
        return trie;
    }

    private static boolean nextArgumentValid(String[] args, int argIndex) {
        return (argIndex + 1 < args.length) &&
               (args[argIndex + 1].length() > 0) &&
               (args[argIndex + 1].charAt(0) != '-');
    }

    public static ParsedArguments parseArguments(String[] args) {
        ParsedArguments parsedArgs = new ParsedArguments();
        int argIndex = 0;

        while(argIndex < args.length) {
            String arg = args[argIndex];

            if("-d".equals(arg)) {
                if(nextArgumentValid(args, argIndex)) {
                    parsedArgs.dictionaryFile = args[argIndex + 1];
                    argIndex += 2;
                }
                else {
                    System.out.println("Expected dictionary file path after -d!");
                    return parsedArgs;
                }
            }
            else if("-r".equals(arg)) {
                if(nextArgumentValid(args, argIndex)) {
                    parsedArgs.reversedDictionaryFile = args[argIndex + 1];
                    argIndex += 2;
                }
                else {
                    System.out.println("Expected reversed dictionary file path after -r!");
                    return parsedArgs;
                }
            }
            else if("-e".equals(arg)) {
                if(nextArgumentValid(args, argIndex)) {
                    String countString = args[argIndex + 1];
                    argIndex += 2;

                    try {
                        parsedArgs.maxErrors = Integer.parseInt(countString);
                    }
                    catch(NumberFormatException ex) {
                        System.out.println("Invalid number specified for maximum errors!");
                        return parsedArgs;
                    }
                }
                else {
                    System.out.println("Expected maximum error count after -e!");
                    return parsedArgs;
                }
            }
            else if("-t".equals(arg)) {
                if(nextArgumentValid(args, argIndex)) {
                    parsedArgs.testFile = args[argIndex + 1];
                    argIndex += 2;
                }
                else {
                    System.out.println("Expected test file path after -r!");
                    return parsedArgs;
                }
            }
            else if("-g".equals(arg)) {
                if(nextArgumentValid(args, argIndex)) {
                    parsedArgs.graphvizFile = args[argIndex + 1];
                    argIndex += 2;
                }
                else {
                    System.out.println("Expected Graphviz DOT file path after -r!");
                    return parsedArgs;
                }
            }
            else if("-c".equals(arg)) {
                parsedArgs.useCache = true;
                argIndex++;
            }
            else if("-v".equals(arg)) {
                parsedArgs.verbose = true;
                argIndex++;
            }
            else {
                System.out.println("Unknown command-line argument!");
                return parsedArgs;
            }
        }

        parsedArgs.valid = (parsedArgs.dictionaryFile != null) &&
                           (parsedArgs.testFile != null) &&
                           (parsedArgs.maxErrors > 0);
        return parsedArgs;
    }

    public static void main(String[] args) throws Exception {
        // Parse and validate the command-line arguments.
        ParsedArguments  parsedArgs = parseArguments(args);

        if(!parsedArgs.valid) {
            System.out.println("Required command-line arguments not supplied!");
            return;
        }

        // Read the dictionaries and build the tries.
        List<String> dictionaryWords = readWordList(parsedArgs.dictionaryFile);
        Trie dictionaryTrie = buildTrie(dictionaryWords);
        Trie reversedDictionaryTrie = null;

        if(parsedArgs.reversedDictionaryFile != null) {
            List<String> reversedDictionaryWords = readWordList(parsedArgs.reversedDictionaryFile);
            reversedDictionaryTrie = buildTrie(reversedDictionaryWords);
        }

        AutomatonCache cache = parsedArgs.useCache ? new SimpleAutomatonCache() : null;
        FuzzyMatching matching = new FuzzyMatching(dictionaryTrie, reversedDictionaryTrie,
                                                   parsedArgs.maxErrors, cache);

        // Find the similar words for each word in the test file.
        long startTime = System.nanoTime();
        int matchingWordCount = 0;
        FileInputStream stream = null;

        try {
            stream = new FileInputStream(parsedArgs.testFile);
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            String line = reader.readLine();

            while(line != null) {
                //List<String> matchingWords = matching.findMatchingWords(line.trim());
                List<String> matchingWords = LevenshteinDistance.findAcceptedWords(dictionaryWords, line.trim(), 2);
                matchingWordCount += matchingWords.size();

                if(parsedArgs.verbose) {
                    System.out.println("Similar words to " + line + ":");

                    for(String word : matchingWords) {
                        System.out.println("    " + word);
                    }
                }

                line = reader.readLine();
            }
        }
        catch(IOException ex) {
            System.out.println("Failed to read test file!");
            return;
        }
        finally {
            if(stream != null) stream.close();
        }

        double duration = (double)(System.nanoTime() - startTime) / 1.0e9;
        System.out.println("Operation completed.");
        System.out.println("Matching words found: " + matchingWordCount);
        System.out.println("Duration: " + duration);
    }
}
