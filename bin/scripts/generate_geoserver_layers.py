import psycopg2
import os

namespace = "NamespaceInfoImpl-21400543:1604535a380:-7ffe"
datastore = "DataStoreInfoImpl--596c0b74:1604c3037af:-7fff"

featuretype_xml_tpl = """<featureType>
  <id>FeatureTypeInfoImpl-%s-wgp</id>
  <name>%s</name>
  <nativeName>%s</nativeName>
  <namespace>
    <id>""" + namespace + """</id>
  </namespace>
  <title>%s</title>
  <abstract>%s</abstract>
  <keywords>
  %s
  </keywords>
  <srs>EPSG:4326</srs>
  <nativeBoundingBox>
  %s
  </nativeBoundingBox>
  <latLonBoundingBox>
    %s
    <crs>EPSG:4326</crs>
  </latLonBoundingBox>
  <projectionPolicy>FORCE_DECLARED</projectionPolicy>
  <enabled>true</enabled>
  <metadata>
    <entry key="cachingEnabled">false</entry>
  </metadata>
  <store class="dataStore">
    <id>""" + datastore + """</id>
  </store>
  <maxFeatures>0</maxFeatures>
  <numDecimals>0</numDecimals>
</featureType>
"""

layer_xml_tpl="""<layer>
  <name>%s</name>
  <id>LayerInfoImpl-%s</id>
  <type>VECTOR</type>
  <defaultStyle>
    <id>%s</id>
  </defaultStyle>
  <styles class="linked-hash-set">
  %s
  </styles>
  <resource class="featureType">
    <id>FeatureTypeInfoImpl-%s-wgp</id>
  </resource>
  <enabled>true</enabled>
  <attribution>
    <logoWidth>0</logoWidth>
    <logoHeight>0</logoHeight>
  </attribution>
</layer>
"""

cur = None

def get_keywords_xml(tablename):
    global cur
    keywords_xml = ''

    layer_keywords_query = 'select tags from "Meta_Layer" where layer_tablename=\'' + tablename + '\''
    cur.execute(layer_keywords_query)
  
    layer_keywords_results = cur.fetchone()[0]
    if layer_keywords_results:
        layer_keywords = layer_keywords_results.split(",")
    else:
        layer_keywords = ["Miscellaneous"]

    for keyword in layer_keywords:
        keywords_xml = keywords_xml + '<string>' + keyword.strip() + '</string>'
    
    return keywords_xml

def get_bounding_box(tablename):    

    global cur
    bbox_xml = ''

    layer_bbox_query = 'select min(st_xMin(__mlocate__topology)), max(st_xMax(__mlocate__topology)), min(st_yMin(__mlocate__topology)), max(st_yMax(__mlocate__topology)) from ' + tablename

    #print layer_bbox_query
    cur.execute(layer_bbox_query)

    bbox = cur.fetchone()

    bbox_xml = bbox_xml + '<minx>' + str(bbox[0]) + '</minx>'
    bbox_xml = bbox_xml + '<maxx>' + str(bbox[1]) + '</maxx>'
    bbox_xml = bbox_xml + '<miny>' + str(bbox[2]) + '</miny>'
    bbox_xml = bbox_xml + '<maxy>' + str(bbox[3]) + '</maxy>'

    return bbox_xml

def create_featuretype_xml(tablename):

    global cur
    layer_info_query = 'select layer_name, layer_description from "Meta_Layer" where layer_tablename=\'' + tablename + '\''
    cur.execute(layer_info_query)

    layer_info = cur.fetchone()
    
    title = layer_info[0].strip("'")

    if layer_info[1]:
        abstract = layer_info[1].strip("'")
    else:
        abstract = title
    
    keywords = get_keywords_xml(tablename)

    bbox = get_bounding_box(tablename)

    featuretype_xml = featuretype_xml_tpl % (tablename, tablename, tablename, title, abstract, keywords, bbox, bbox)
    
    featuretype_xml_file = open("featuretype.xml", "w")
    featuretype_xml_file.write(featuretype_xml)
    featuretype_xml_file.close()

def create_styles_xml(tablename):
    global cur
    # cont_type = ['bigint', 'integer', 'numeric', 'smallint', 'double precision', 'real']
    # 'numeric' type data is not yet supported by geoserver for vector tiles. hence disabling
    # style generation for such columns
    cont_type = ['bigint', 'integer', 'smallint', 'double precision', 'real']

    styles = ''
    colname_datatype_query = "select column_name, data_type from information_schema.columns where table_name = '" + tablename + "'"
    cur.execute(colname_datatype_query)
    resultset = cur.fetchall()
    for row in resultset:
        if not row[0].startswith('__mlocate__') and (row[1].startswith('character') or (cont_type.count(row[1]) == 1)):
            styles = styles + '<style>\n  <id>' + tablename + '_' + row[0] + '</id>\n</style>'

    return styles            

def create_layer_xml(tablename):

    global cur
    layer_info_query = 'select color_by, title_column, layer_type from "Meta_Layer" where layer_tablename=\'' + tablename + '\''
    cur.execute(layer_info_query)

    row = cur.fetchone()

    layer_type =  row[2]
    default_style = ''

    if row[0] == None:
        print 'No color_by specified for table: ' + tablename
        return;
    if layer_type == 'MULTIPOLYGON':
        color_by = row[0].strip("'");
        default_style = tablename + "_" + color_by
    elif layer_type == 'POINT':
        color_by = row[0].strip("'");
	if color_by:
         	default_style = tablename + "_" + color_by
	else:
        	title_column = row[1].strip("'");
        	default_style = tablename + "_" + title_column

    styles = create_styles_xml(tablename)

    layer_xml = layer_xml_tpl % (tablename, tablename, default_style, styles, tablename)        

    layer_xml_file = open("layer.xml", "w")
    layer_xml_file.write(layer_xml)
    layer_xml_file.close()

def generate_layer_xml(tables, geoserver_data_dir_path):
  global cur
  try:
    conn = psycopg2.connect("dbname='ibp' user='biodiv' host='localhost' password='prharasr'")
  except:
    print "unable to connect to the database"

  cur = conn.cursor()

  for tablename in tables:
    print 'layer: ' + tablename
    os.system("mkdir -p " + "layers/" + tablename)
    create_featuretype_xml(tablename)
    create_layer_xml(tablename)
    os.system("mv layer.xml featuretype.xml layers/" + tablename)
    os.system("mv layers/" + tablename + " " + geoserver_data_dir_path + "/workspaces/biodiv/ibp/")
