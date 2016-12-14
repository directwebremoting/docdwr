
DOCDWR
------

This project contains the documentation for DWR.
The documentation is stored in HTML files suitable for editing in any HTML
editor. There is a publishing process that prepares the files (by adding
certain header/footer/menu elements)


License
-------

This project is not currently open sourced. All rights to the documentation
provided is preserved by its authors. This means that you do not have the right
to redistribute, build on, copy or publish any of the material in this project.
The official publishing location is under http://directwebremoting.org/


How to build files for publishing
---------------------------------
Just run the template/main/java/org.directwebremoting.doc.template.Generate 
class. It requires a program argument of the location of the docs. That builds 
out the docs in a target/publish dir.


Editing Rules
-------------
In order to ensure that the publishing process works properly, there are some
rules for editing the documentation. MUST means that failure to follow this rule
will break the publishing process, possibly in hard to detect ways.
SHOULD means that failure to follow the rule is likely to thwart future changes
to the publishing process.
- All links MUST be relative
- All pages SHOULD have an HTML5 doctype: <!DOCTYPE html>
- All pages MUST be stored with a .html file extension
- All elements/attributes SHOULD be valid html5
- In addition to normal html5 rules, the documents SHOULD be well-formed XML
  (although they should not include an XML declaration <?xml version="1.0"...
  and they do not need to be valid beyond the html5 rules)
  This may help us process the documents using XSL or similar in the future
- All directories MUST contain an index.html file that links to all of the
  HTML files within that directory
- All images/css/js should be stored in the dwr/media directory
- Page titles SHOULD be succinct, particularly in index.html files, because they
  are used in the breadcrumbs and menu structure

- Keep to the same formatting style as the html around you. In general
  this means that most content will be formatted with a paragraph on a single
  line with open and closing tags on the same line. Paragraphs are then
  separated by a single blank line. This style means that the HTML is fairly
  readable in any text editor that supports word wrapping.
  In general elements other than tables are not indented. When indenting use
  2 space characters and no tabs
- Please keep the HTML very simple
  For the most part p/h1/h2/h3/a/strong/pre/code are all the tags you should need
- We have defined 3 extra meta elements:
  - <meta name='alias' content='/path/to/old/content'>
    This allows us to generate apache.conf files that reduce 404s. This line
    is in effect a 'COME_FROM'. And you thought GOTO was evil ....
  - <meta name='weight' content='-5'>
    Heavier items (bigger numbers) sink down menus. The default is 0
  - <meta name='navTitle' content='Short Desc'>
    The side navigation needs to be compact when the actual page title may be
    better being more expressive. This allows us to override the official title
- If you want to move a page from one location to another, please:
  - Ensure that all inbound links point to the new location
  - In the header of the moved document, add a line that points to the old location
    This enables us to create an Apache config file that prevents needless 404s


Templating Process
------------------

The templating process reads the source files in docs/web, and then writes them
to target/template with additional templating.
A source page is of roughly the following format

HEADER LINES
 *</head> *
 *<body.*
BODY LINES
 *</body> *
 *</html>

There are then 3 insertion points.
- At the end of the header (used for inserting additional css/scripts/etc.)
- At the top of the body (header/breadcrumbs)
- At the bottom of the body (footer/menu/scripts/etc)

There are, then 7 parts to the output document
- Page Header
    Everything in the source page up to (but not including) </head>
    Stored in the Page.header member variable
- Template Header Insert
    Fetched from the getTemplateHeaderInsert() method
- Page Neck
    Everything in the source page from </head> to <body> (inclusive)
    Stored in the Page.neck member variable
- Template PreBody Insert
    Fetched from the getTemplatePreBodyInsert() method
- Page Body
    Everything from <body> to </body> (exclusive)
    Stored in the Page.body member variable
- Template Post Body Insert
    Fetched from the getTemplatePostBodyInsert() method
- Page Close
    Everything from </body> (inclusive) to the end of the document
    Stored in the Page.close member variable
    Probably equal to the fixed string </body>\n</html>\m

In addition, the following details are extracted for use in templating and
redirect generation:
- Document title <title>***</title>
- Page Aliases <meta name="alias" value="PATH-TO-OLD-LOCATION">
- File path
