package org.directwebremoting.doc.template;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.directwebremoting.fsguide.FileSystemGuide;
import org.directwebremoting.fsguide.Visitor;
import org.directwebremoting.util.CopyUtils;

/**
 *
 * @author Joe Walker [joe at getahead dot ltd dot uk]
 */
public class Generate
{
    /**
     *
     */
    private static class Page implements Comparable<Page>
    {
        StringBuilder header = new StringBuilder();
        StringBuilder neck = new StringBuilder();
        StringBuilder body = new StringBuilder();
        StringBuilder close = new StringBuilder();

        final String path;
        final File source;

        String title;
        String navTitle;
        int weight = 0;

        List<String> aliases = new ArrayList<String>();

        Page parent;
        Set<Page> children = new TreeSet<Page>();

        public Page(String path, File source)
        {
            this.path = path;
            this.source = source;
        }

        @Override
        public String toString()
        {
            return "Page[path=" + path + ", title='" + navTitle + "', pathToRoot=" + getPathToRoot() + ", parent=" + (parent == null ? "null" : parent.path) + "]";
        }

        public String getLink(Page from)
        {
            return getLink(from, navTitle, false);
        }

        public String getLink(Page from, String text, boolean preventCurrentLink)
        {
            if (this == from && !preventCurrentLink)
            {
                return "<a href='" + source.getName() + "' class='currentLink'>" + text + "</a>";
            }

            return "<a href='" + from.getPathToRoot() + path + "'>" + text + "</a>";
        }

        public int compareTo(Page that)
        {
            if (this.weight == that.weight)
            {
                return this.navTitle.compareTo(that.navTitle);
            }
            else if (this.weight > that.weight)
            {
                return 1;
            }
            else
            {
                return -1;
            }
        }

        public String getPathToRoot()
        {
            StringBuilder builder = new StringBuilder();
            Page current = this.parent;

            if (null != current && path.endsWith("index.html"))
            {
               builder.append("../");
            }
            while (null != current)
            {
                current = current.parent;
                if (null != current)
                {
                   builder.append("../");
                }
            }
            return builder.toString();
        }
    }

    private static final String ROOT = "ROOT";

    private enum Reading
    {
        Header, Neck, Body, Close
    }

    /**
     * The root path to the documentation is a required argument:
     *   I.E - C:\dev\workspaces\openSource\docdwr on Windows.
     *       - /dev/workspaces/openSource/docdwr on *ix.
     */
    public static void main(String[] args) throws IOException
    {
        if (args.length == 0) {
            System.out.println("The root path to the documentation on your file system is a required program argument. I.E. /Users/joe/Projects/directwebremoting/docdwr");
            System.exit(0);
        }
        String root = args[0];
        Map<String, Page> pages = readInput(root + "/docs/web/dwr");

        writeHtmlOutput(pages, root + "/target/publish/");
        copyStatic(root + "/docs/web/dwr", root + "/target/publish", new String[] {
            "png", "gif", "jpg", "css", "pdf", "dtd", "xsd", "js"
        });

        writeApacheConfOutput(pages, root + "/target/dwr.conf");
    }

    /**
     *
     */
    private static void writeApacheConfOutput(Map<String, Page> pages, String base)
    {
        PrintStream out = System.out;
        for (Page page : pages.values())
        {
            for (String alias : page.aliases)
            {
                out.print("Redirect ");
                out.print(alias);
                out.print(" /dwr/");
                out.print(page.path);
                out.println();
            }
        }
    }

    /**
     *
     */
    private static void writeHtmlOutput(Map<String, Page> pages, String base) throws IOException
    {
        for (Page page : pages.values())
        {
            File destination = new File(base + page.path);
            destination.getParentFile().mkdirs();

            PrintWriter out = new PrintWriter(new FileWriter(destination));
            out.print(page.header);
            out.print(getTemplateHeaderInsert(page));
            out.print(page.neck);
            out.print(getTemplatePreBodyInsert(page));
            out.print("<div id='content'>\n");
            out.print(page.body);
            out.print("</div>\n");
            out.print(getTemplatePostBodyInsert(page, pages.get(ROOT)));
            out.print(page.close);
            out.close();
        }
    }

    /**
     *
     */
    private static Map<String, Page> readInput(final String base)
    {
        final Map<String, Page> pages = new TreeMap<String, Page>();

        FileSystemGuide guide = new FileSystemGuide(new File(base));
        guide.visit(new Visitor()
        {
            public void visitFile(File file)
            {
                String path = stripBase(file, base);

                if (path.endsWith(".html"))
                {
                    Page page = new Page(path, file);
                    pages.put(path, page);

                    try
                    {
                        BufferedReader in = new BufferedReader(new FileReader(file));
                        Reading reading = Reading.Header;

                        while (true)
                        {
                            String line = in.readLine();
                            if (line == null)
                            {
                                break;
                            }

                            switch (reading)
                            {
                            case Header:
                                if (line.matches(" *</head> *"))
                                {
                                    reading = Reading.Neck;
                                    page.neck.append(line);
                                    page.neck.append("\n");
                                }
                                else
                                {
                                    page.header.append(line);
                                    page.header.append("\n");

                                    Matcher titleMatcher = title.matcher(line);
                                    if (titleMatcher.find())
                                    {
                                        page.title = titleMatcher.group(1);
                                        if (page.navTitle == null)
                                        {
                                            page.navTitle = page.title;
                                        }
                                    }

                                    Matcher metaMatcher = meta.matcher(line);
                                    if (metaMatcher.find())
                                    {
                                        String name = metaMatcher.group(1);
                                        String value = metaMatcher.group(2);
                                        if ("alias".equals(name))
                                        {
                                            page.aliases.add(value);
                                        }
                                        else if ("navTitle".equals(name))
                                        {
                                            page.navTitle = value;
                                        }
                                        else if ("weight".equals(name))
                                        {
                                            page.weight = Integer.parseInt(value);
                                        }
                                    }
                                }
                                break;

                            case Neck:
                                page.neck.append(line);
                                page.neck.append("\n");
                                if (line.matches(" *<body.*"))
                                {
                                    reading = Reading.Body;
                                }
                                break;

                            case Body:
                                if (line.matches(" *</body> *"))
                                {
                                    reading = Reading.Close;
                                    page.close.append(line);
                                    page.close.append("\n");
                                }
                                else
                                {
                                    page.body.append(line);
                                    page.body.append("\n");
                                }
                                break;

                            case Close:
                                page.close.append(line);
                                page.close.append("\n");
                                break;
                            }
                        }
                    }
                    catch (IOException ex)
                    {
                        log.warn("Parse failed", ex);
                    }
                }
            }

            public boolean visitDirectory(File directory)
            {
                if (directory.getName().startsWith("_vti"))
                {
                    return false;
                }

                return true;
            }
        });

        Page root = null;
        for (Page page : pages.values())
        {
            String parentDir = page.path.endsWith("index.html") ?
                    page.source.getParentFile().getParent() :
                    page.source.getParent();

            File parentFile = new File(parentDir, "/index.html");
            String key = stripBase(parentFile, base);
            page.parent = pages.get(key);

            if (page.parent == null)
            {
                if (root == null)
                {
                    root = page;
                }
                else
                {
                    log.error("More than 1 candidate for tree root:");
                    log.error("- " + root);
                    log.error("- " + page);
                }
            }
            else
            {
                page.parent.children.add(page);
            }
        }

        pages.put(ROOT, root);
        return pages;
    }

    /**
     *
     */
    private static String stripBase(File file, String base)
    {
        String path = file.getAbsolutePath();
        return replaceWindowsFileSepWithUnix(path.substring(base.length() + 1));
    }

    /**
     * Private utility to get things to work on Windows.
     * @return
     */
    private static String replaceWindowsFileSepWithUnix(String path) {
        return path.replace('\\', '/');
    }

    /**
     *
     */
    private static void copyStatic(final String source, final String dest, final String[] extensions)
    {
        FileSystemGuide guide = new FileSystemGuide(new File(source));
        guide.visit(new Visitor()
        {
            public void visitFile(File file)
            {
                String path = file.getAbsolutePath();

                String extra = path.substring(source.length());
                File toFile = new File(dest + extra);

                boolean matching = false;
                for (String extension : extensions)
                {
                    if (path.endsWith("." + extension))
                    {
                        matching = true;
                    }
                }

                if (matching)
                {
                    try
                    {
                        toFile.getParentFile().mkdirs();
                        CopyUtils.copy(new FileInputStream(file), new FileOutputStream(toFile));
                    }
                    catch (IOException ex)
                    {
                        log.warn("Failed to copy: " + file.getAbsolutePath(), ex);
                    }
                }
            }

            public boolean visitDirectory(File directory)
            {
                if (directory.getName().startsWith("_vti"))
                {
                    return false;
                }

                if (directory.getName().startsWith(".svn"))
                {
                    return false;
                }

                return true;
            }
        });
    }

    /**
     *
     */
    private static String getTemplateHeaderInsert(Page page)
    {
        StringBuilder builder = new StringBuilder();

        builder.append("  <script type='text/javascript' src='");
        builder.append(page.getPathToRoot());
        builder.append("media/dojo.js");
        builder.append("'> </script>\n");
        builder.append("  <script type='text/javascript' src='");
        builder.append(page.getPathToRoot());
        builder.append("media/menu.js");
        builder.append("'> </script>\n");

        return builder.toString();
    }

    /**
     *
     */
    private static final String STANDARD_HEADER =
        "<div id='header'><a href='http://directwebremoting.org/dwr/'>Direct Web Remoting</a></div>\n" +
        "<div id=pagelinks>\n" +
          "<div class='navlinks'>[ \n" +
             "<a href='/dwr' title='Easy Ajax for Java'>DWR</a> | \n" +
             "<a href='/blog/joe' title='Joe&#039;s Blog about DWR and Ajax'>Blog</a>\n" +
           " ]\n" +
            "<form method='GET' action='http://www.google.com/search' id='search-form'>\n" +
              "<span id='search'>\n" +
                "<input class='form-text' type='text' id='search-box' name='as_q' size='20' maxlength='255'>\n" +
                "<input type='hidden' name='as_sitesearch' value='directwebremoting.org'/>\n" +
                "<input class='form-submit' type='submit' value='Google'/>\n" +
              "</span>\n" +
            "</form>\n" +
          "</div>\n";

    /**
     *
     */
    private static String getTemplatePreBodyInsert(Page page)
    {
        StringBuilder breadcrumbs = new StringBuilder();
        Page history = page;

        while (history != null)
        {
            if (breadcrumbs.length() != 0)
            {
                breadcrumbs.insert(0, " &#x00BB; ");
            }

            breadcrumbs.insert(0, history.getLink(page));
            history = history.parent;
        }

        return STANDARD_HEADER + "<div id='breadcrumbs'>" + breadcrumbs + "</div></div>\n";
    }

    /**
     *
     */
    private static String getTemplatePostBodyInsert(Page base, Page root)
    {
        StringBuilder menu = new StringBuilder();
        menu.append("<div class='menu'>\n");
        menu.append("<ul id='nav'>\n");

        menu.append("<li class='noChildren'>");
        menu.append(root.getLink(base, "Home", false));
        menu.append("</li>\n");

        for (Page child : root.children)
        {
            addMenuOptions(menu, base, child);
        }

        menu.append("</ul></div>\n");

        return menu.toString();
    }

    /**
     * &#x2192; is right arrow, &#x2193; is down arrow
     */
    //private static final String ARROW = "&nbsp;&#x2193;";
    private static final String ARROW = "&nbsp;...";

    /**
     *
     */
    private static void addMenuOptions(StringBuilder menu, Page base, Page page)
    {
        boolean hasChildren = (page.children.size() > 0);
        if (hasChildren)
        {
            menu.append("<li class='hasChildren'>");
            menu.append(page.getLink(base, page.navTitle + ARROW, true));

            menu.append("<ul>\n");

            menu.append("<li class='noChildren'>");
            menu.append(page.getLink(base, "Index", false));
            menu.append("</li>\n");

            for (Page child : page.children)
            {
                addMenuOptions(menu, base, child);
            }

            menu.append("</ul>\n");

            menu.append("</li>\n");
        }
        else
        {
            menu.append("<li class='noChildren'>");
            menu.append(page.getLink(base));
            menu.append("</li>\n");
        }
    }

    private static final Pattern title = Pattern.compile(" *<title>(.*)</title> *");

    private static final Pattern meta = Pattern.compile(" *<meta name=['\"](.*)['\"] content=['\"](.*)['\"]");

    /**
     * The log stream
     */
    private static final Log log = LogFactory.getLog(Generate.class);
}
