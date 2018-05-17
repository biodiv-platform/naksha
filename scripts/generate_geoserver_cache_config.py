import psycopg2
import os

workspace_name = "biodiv"

header_xml_tpl = """<GeoServerTileLayer>
  <id>LayerInfoImpl-%s</id>
  <enabled>true</enabled>
  <inMemoryCached>true</inMemoryCached>
  <name>""" + workspace_name + """:%s</name>"""

format_xml_tpl = """\n  <mimeFormats>
    <string>image/png</string>
    <string>image/jpeg</string>
    <string>application/json;type=geojson</string>
    <string>application/x-protobuf;type=mapbox-vector</string>
  </mimeFormats>
  <gridSubsets>
    <gridSubset>
      <gridSetName>EPSG:900913</gridSetName>
      <extent>
        <coords>
          <double>-2.003750834E7</double>
          <double>-2.003750834E7</double>
          <double>2.003750834E7</double>
          <double>2.003750834E7</double>
        </coords>
      </extent>
    </gridSubset>
    <gridSubset>
      <gridSetName>EPSG:4326</gridSetName>
      <extent>
        <coords>
          <double>-180.0</double>
          <double>-90.0</double>
          <double>180.0</double>
          <double>90.0</double>
        </coords>
      </extent>
    </gridSubset>
  </gridSubsets>
  <metaWidthHeight>
    <int>4</int>
    <int>4</int>
  </metaWidthHeight>
  <expireCache>0</expireCache>
  <expireClients>0</expireClients>
"""

styles_xml_tpl = """  <parameterFilters>
    <styleParameterFilter>
      <key>STYLES</key>
      <defaultValue></defaultValue>
      <availableStyles class="sorted-set">
%s      </availableStyles>
      <defaultStyle>lyr_100_india_cpa_vill_name</defaultStyle>
    </styleParameterFilter>
  </parameterFilters>
"""

footer_xml_tpl = """  <gutter>0</gutter>
</GeoServerTileLayer>
"""

def create_cache_xml(tablename):
    # cont_type = ['bigint', 'integer', 'numeric', 'smallint', 'double precision', 'real']
    # 'numeric' type data is not yet supported by geoserver for vector tiles. hence disabling
    # style generation for such columns
    cont_type = ['bigint', 'integer', 'smallint', 'double precision', 'real']
    styles = []
    colname_datatype_query = "select column_name, data_type from information_schema.columns where table_name = '" + tablename + "'"
    cur.execute(colname_datatype_query)
    resultset = cur.fetchall()
    for row in resultset:
        if not row[0].startswith('__mlocate__') and (row[1].startswith('character') or (cont_type.count(row[1]) == 1)):
            #styles = styles + '<style>\n  <id>' + tablename + '_' + row[0] + '</id>\n</style>'
            styles = styles + [tablename + '_' + row[0]]

    styles.sort()
    style_conf = ''
    for style in styles:
        style_conf = style_conf + ('        <string>%s</string>\n' % style)

    filename = 'LayerInfoImpl-' + tablename + '.xml'
    conf_xml_file = open(filename, 'w')
    conf_xml_file.write(header_xml_tpl % (tablename, tablename))
    conf_xml_file.write(format_xml_tpl)
    conf_xml_file.write(styles_xml_tpl % style_conf)
    conf_xml_file.write(footer_xml_tpl)
    conf_xml_file.close()

tables = []

try:
    conn = psycopg2.connect("dbname='ibp' user='biodiv' host='localhost' password='biodiv'")
except:
    print "unable to connect to the database"

cur = conn.cursor()

os.system("mkdir " + "cache_config")
for tablename in tables:
    print 'layer: ' + tablename
    create_cache_xml(tablename)
    os.system("mv LayerInfoImpl-* cache_config/")
