package org.directwebremoting.doc.template;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.directwebremoting.fsguide.FileSystemGuide;
import org.directwebremoting.fsguide.Visitor;

/**
 * 
 * @author Joe Walker [joe at getahead dot ltd dot uk]
 */
public class Generate
{
    private static class Page
    {
        String path;
        String title;
        String parent;
        String weight;
        List<String> aliases = new ArrayList<String>();
        String body;

        @Override
        public String toString()
        {
            return "Page[path=" + path + ", title=" + title + ", parent=" + parent + ", weight=" + weight + ", aliases=" + aliases + "]";
        }
    }

    public static void main(String[] args)
    {
        final String root = "/Users/joe/Projects/directwebremoting/docdwr/docs/web";
        Map<String, Page> pages = readInput(root);

        for (Map.Entry<String, Page> entry : pages.entrySet())
        {
            log.info(entry.getKey() + " =");
            log.info("- " + entry.getValue());
        }

        
    }

    /**
     * 
     */
    private static Map<String, Page> readInput(final String root)
    {
        final Map<String, Page> pages = new HashMap<String, Page>();

        FileSystemGuide guide = new FileSystemGuide(new File(root));
        guide.visit(new Visitor()
        {
            public void visitFile(File file)
            {
                String path = file.getAbsolutePath();
                path = path.substring(root.length());

                if (path.endsWith(".html"))
                {
                    Page page = new Page();
                    page.path = path;
                    pages.put(path, page);

                    try
                    {
                        BufferedReader in = new BufferedReader(new FileReader(file));
                        StringBuilder body = new StringBuilder();
                        boolean inBody = false;

                        while (true)
                        {
                            String line = in.readLine();
                            if (line == null)
                            {
                                log.warn("Expected </body>");
                                break;
                            }

                            if (inBody)
                            {
                                if (line.matches(" *</body> *"))
                                {
                                    break;
                                }
                                body.append(line);
                                body.append("\n");
                            }
                            else
                            {
                                if (line.matches(" *<body> *"))
                                {
                                    inBody = true;
                                    continue;
                                }

                                Matcher titleMatcher = title.matcher(line);
                                if (titleMatcher.find())
                                {
                                    page.title = titleMatcher.group(1);
                                }

                                Matcher metaMatcher = meta.matcher(line);
                                if (metaMatcher.find())
                                {
                                    String name = metaMatcher.group(1);
                                    String value = metaMatcher.group(2);

                                    if ("nid".equals(name))
                                    {
                                        // page.nid = value;
                                    }
                                    else if ("parent".equals(name))
                                    {
                                        page.parent = value;
                                    }
                                    else if ("weight".equals(name))
                                    {
                                        page.weight = value;
                                    }
                                    else if ("filename".equals(name))
                                    {
                                        // page.filename = value;
                                    }
                                    else if ("alias".equals(name))
                                    {
                                        page.aliases.add(value);
                                    }
                                }
                            }
                        }

                        page.body = body.toString();
                    }
                    catch (IOException ex)
                    {
                        log.warn("Parse failed", ex);
                    }

                    log.info("Found " + path);
                }
            }

            public boolean visitDirectory(File directory)
            {
                return true;
            }
        });
        return pages;
    }

    private static final Pattern title = Pattern.compile(" *<title>(.*)</title> *");

    private static final Pattern meta = Pattern.compile(" *<meta name=['\"](.*)['\"] content=['\"](.*)['\"]");

    /**
     * The log stream
     */
    private static final Log log = LogFactory.getLog(Generate.class);
}
