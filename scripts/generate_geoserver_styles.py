import psycopg2
import random
import math
import os

#XML file content
xml_tpl = """<style>
  <id>%s</id>
  <name>%s</name>
  <filename>%s</filename>
  <jsonfilename>%s</jsonfilename>
</style>
"""

header_tpl_mgl = """{
  "version": 8,
  "sources": {
    "geoserver": {
      "type": "vector",
      "scheme": "tms",
      "tiles": ["http://localhost:6792/geoserver/gwc/service/tms/1.0.0/biodiv:%s@EPSG%%3A900913@pbf/{z}/{x}/{y}.pbf"]
    }
  },
  "layers": [{
    "id": "%s",
    "type": "%s",
    "source": "geoserver",
    "source-layer": "%s",
    "paint": """

paint_tpl_fill_mgl = """{
      "fill-outline-color": "#aaaaaa",
      "fill-color": {
        "property": "%s",
        "type": "%s",
        "stops": ["""

paint_tpl_circle_mgl = """{
      "circle-radius": 5,
      "circle-color": {
        "property": "%s",
        "type": "%s",
        "stops": ["""

paint_tpl_line_mgl = """{
      "line-width": 1,
      "line-color": {
        "property": "%s",
        "type": "%s",
        "stops": ["""

polygon_rule_tpl_mgl = """
          [%s, "#%s"],"""

point_rule_tpl_mgl = """
          [%s, "#%s"],"""

line_rule_tpl_mgl = """
          [%s, "#%s"],"""

footer_tpl_mgl = """
        ]
      }
    }
  }]
}"""

header_tpl = """<?xml version="1.0" encoding="ISO-8859-1"?>
<StyledLayerDescriptor version="1.0.0" 
    xsi:schemaLocation="http://www.opengis.net/sld StyledLayerDescriptor.xsd" 
    xmlns="http://www.opengis.net/sld" 
    xmlns:ogc="http://www.opengis.net/ogc" 
    xmlns:xlink="http://www.w3.org/1999/xlink" 
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <NamedLayer>
    <Name>Attribute-based polygon</Name>
    <UserStyle>
      <Title><![CDATA[%s]]></Title>
      <FeatureTypeStyle>
"""

point_rule_tpl = """
<Rule>
  <Name><![CDATA[%s]]></Name>
  <Title><![CDATA[%s]]></Title>
  <ogc:Filter>
    <ogc:And>
      <ogc:PropertyIsGreaterThanOrEqualTo>
        <ogc:PropertyName>%s</ogc:PropertyName>
        <ogc:Literal>%s</ogc:Literal>
      </ogc:PropertyIsGreaterThanOrEqualTo>
      <ogc:PropertyIsLessThan>
        <ogc:PropertyName>%s</ogc:PropertyName>
        <ogc:Literal>%s</ogc:Literal>
      </ogc:PropertyIsLessThan>
    </ogc:And>
  </ogc:Filter>

 <PointSymbolizer>
     <Graphic>
       <Mark>
         <WellKnownName>circle</WellKnownName>
         <Fill>
            <CssParameter name="fill">
            #<ogc:Function name="env">
                <ogc:Literal>%s</ogc:Literal>
                <ogc:Literal>%s</ogc:Literal>          
            </ogc:Function>
            </CssParameter>
         </Fill>
        <Stroke>
           <CssParameter name="stroke">
                #<ogc:Function name="env">
                    <ogc:Literal>stroke</ogc:Literal>
                    <ogc:Literal>%s</ogc:Literal>          
                </ogc:Function>
           </CssParameter>
           <CssParameter name="stroke-width">
                <ogc:Function name="env">
                    <ogc:Literal>stroke-width</ogc:Literal>
                    <ogc:Literal>%s</ogc:Literal>          
                </ogc:Function>
           </CssParameter>
    </Stroke>
       </Mark>
       <Size>
          <ogc:Function name="env">
           <ogc:Literal>size</ogc:Literal>
           <ogc:Literal>%s</ogc:Literal>          
           </ogc:Function>
       </Size>
     </Graphic>
   </PointSymbolizer>
</Rule>
"""

polygon_rule_tpl = """
<Rule>
  <Name><![CDATA[%s]]></Name>
  <Title><![CDATA[%s]]></Title>
  <ogc:Filter>
    <ogc:And>
      <ogc:PropertyIsGreaterThanOrEqualTo>
        <ogc:PropertyName>%s</ogc:PropertyName>
        <ogc:Literal>%s</ogc:Literal>
      </ogc:PropertyIsGreaterThanOrEqualTo>
      <ogc:PropertyIsLessThan>
        <ogc:PropertyName>%s</ogc:PropertyName>
        <ogc:Literal>%s</ogc:Literal>
      </ogc:PropertyIsLessThan>
    </ogc:And>
  </ogc:Filter>
  <PolygonSymbolizer>
    <Fill>
      <CssParameter name="fill">
        #<ogc:Function name="env">
           <ogc:Literal>%s</ogc:Literal>
           <ogc:Literal>%s</ogc:Literal>          
         </ogc:Function>
      </CssParameter>
    </Fill>
    <Stroke>
           <CssParameter name="stroke">
                #<ogc:Function name="env">
                    <ogc:Literal>stroke</ogc:Literal>
                    <ogc:Literal>%s</ogc:Literal>          
                </ogc:Function>
           </CssParameter>
           <CssParameter name="stroke-width">
                <ogc:Function name="env">
                    <ogc:Literal>stroke-width</ogc:Literal>
                    <ogc:Literal>%s</ogc:Literal>          
                </ogc:Function>
           </CssParameter>
    </Stroke>
  </PolygonSymbolizer>
</Rule>
"""

point_cat_rule_tpl = """
<Rule>
  <Name><![CDATA[%s]]></Name>
  <Title><![CDATA[%s]]></Title>
  <ogc:Filter>
      <ogc:PropertyIsEqualTo>
        <ogc:PropertyName>%s</ogc:PropertyName>
        <ogc:Literal><![CDATA[%s]]></ogc:Literal>
      </ogc:PropertyIsEqualTo>
  </ogc:Filter>

   <PointSymbolizer>
     <Graphic>
       <Mark>
         <WellKnownName>circle</WellKnownName>
         <Fill>
            <CssParameter name="fill">
            #<ogc:Function name="env">
                <ogc:Literal>%s</ogc:Literal>
                <ogc:Literal>%s</ogc:Literal>          
            </ogc:Function>
            </CssParameter>
         </Fill>

        <Stroke>
           <CssParameter name="stroke">
                #<ogc:Function name="env">
                    <ogc:Literal>stroke</ogc:Literal>
                    <ogc:Literal>%s</ogc:Literal>          
                </ogc:Function>
           </CssParameter>
           <CssParameter name="stroke-width">
                <ogc:Function name="env">
                    <ogc:Literal>stroke-width</ogc:Literal>
                    <ogc:Literal>%s</ogc:Literal>          
                </ogc:Function>
           </CssParameter>
         </Stroke>
       </Mark>
        <Size>
          <ogc:Function name="env">
           <ogc:Literal>size</ogc:Literal>
           <ogc:Literal>%s</ogc:Literal>          
           </ogc:Function>
       </Size>
     </Graphic>
   </PointSymbolizer>
</Rule>
"""

polygon_cat_rule_tpl = """
<Rule>
  <Name><![CDATA[%s]]></Name>
  <Title><![CDATA[%s]]></Title>
  <ogc:Filter>
      <ogc:PropertyIsEqualTo>
        <ogc:PropertyName>%s</ogc:PropertyName>
        <ogc:Literal><![CDATA[%s]]></ogc:Literal>
      </ogc:PropertyIsEqualTo>
  </ogc:Filter>
  <PolygonSymbolizer>
    <Fill>
      <CssParameter name="fill">
        #<ogc:Function name="env">
           <ogc:Literal>%s</ogc:Literal>
           <ogc:Literal>%s</ogc:Literal>          
         </ogc:Function>
      </CssParameter>
    </Fill>
    <Stroke>
          <CssParameter name="stroke">
                #<ogc:Function name="env">
                    <ogc:Literal>stroke</ogc:Literal>
                    <ogc:Literal>%s</ogc:Literal>          
                </ogc:Function>
           </CssParameter>
           <CssParameter name="stroke-width">
                <ogc:Function name="env">
                    <ogc:Literal>stroke-width</ogc:Literal>
                    <ogc:Literal>%s</ogc:Literal>          
                </ogc:Function>
           </CssParameter>
    </Stroke>
  </PolygonSymbolizer>
</Rule>
"""


footer_tpl = """
      </FeatureTypeStyle>
    </UserStyle>
  </NamedLayer>
</StyledLayerDescriptor>
"""


golden_ratio_conjugate = 0.618033988749895

def get_rand_color():
    rnd = random.randint(150, 400)
    r = math.floor(rnd * golden_ratio_conjugate)

    rnd = random.randint(150, 400)
    g = math.floor(rnd * golden_ratio_conjugate)

    rnd = random.randint(150, 400)
    b = math.floor(rnd * golden_ratio_conjugate)

    return '%x%x%x' % (int(r), int(g), int(b))


color_schemes = [['ffff7e', 'f9d155', 'f1a430', 'a75118', '6c0000'],
['f8f6f9', 'cfc4d1', 'a691b4', '886296', '61397f'],
['eee4e5', 'cdaaea', 'bd82f8', '9c55f1', '8129fa']]

def get_bin_size(minimum, maximum, bincount):
    size = (maximum - minimum)/bincount
    return size

def create_style_file_xml(tablename, property_name, sld_filename, json_filename):    
    xml_filename = tablename + '_' + property_name + '.xml'
    xml_file = open(xml_filename, 'w')
    xml_id = tablename + '_' + property_name
    xml = xml_tpl % (xml_id, xml_id, sld_filename, json_filename)
    xml_file.write(xml)
    xml_file.close()

def create_mgl_rule(key, value, layer_type, col_type, isLastRule=False):
    if col_type.startswith('character'):
      # print key, type(key);
      key = '"%s"' % key

    if layer_type == 'MULTIPOLYGON':
        rule_mgl = polygon_rule_tpl_mgl % (key, value)
    elif layer_type == 'POINT' or layer_type == 'MULTIPOINT':
        rule_mgl = point_rule_tpl_mgl % (key, value)
    elif layer_type == 'MULTILINESTRING':
        rule_mgl = line_rule_tpl_mgl % (key, value)

    if isLastRule:
        # remove trailing comma
        rule_mgl = rule_mgl[:-1]
    return rule_mgl

def create_style_files(tablename, property_title, property_name, property_min, property_max, bincount, layer_type, col_type):
    color_scheme = color_schemes[random.randint(0, len(color_schemes)-1)]
    sld_filename = tablename + '_' + property_name + '.sld'
    json_filename = tablename + '_' + property_name + '.json'

    create_style_file_xml(tablename, property_name, sld_filename, json_filename)

    sld_file = open(sld_filename, 'w')
    json_file = open(json_filename, 'w')
    size = get_bin_size(property_min, property_max, bincount)
    mi = property_min
    mx = property_min + size
    
    sld_file.write(header_tpl % (property_title))

    if layer_type == 'MULTIPOLYGON':
    	json_file.write(header_tpl_mgl % (tablename, tablename, 'fill', tablename))
        paint_prop = paint_tpl_fill_mgl % (property_name, 'interval')
    elif layer_type == 'POINT' or layer_type == 'MULTIPOINT':
    	json_file.write(header_tpl_mgl % (tablename, tablename, 'circle', tablename))
        paint_prop = paint_tpl_circle_mgl % (property_name, 'interval')
    elif layer_type == 'MULTILINESTRING':
      json_file.write(header_tpl_mgl % (tablename, tablename, 'line', tablename))
      paint_prop = paint_tpl_line_mgl % (property_name, 'interval')
    json_file.write(paint_prop)

    for i in range(1, bincount+1):
        rule_name = str(mi) + ' to ' + str(mx)
        colr_code = 'c' + str(i)
        colr_hex = color_scheme[i-1]

        if layer_type == 'MULTIPOLYGON':
            rule = polygon_rule_tpl % (rule_name, rule_name, property_name, str(mi), property_name, str(mx), colr_code, colr_hex, 'aaaaaa', 1)
            sld_file.write(rule)
        elif layer_type == 'POINT':
            rule = point_rule_tpl % (rule_name, rule_name, property_name, str(mi), property_name, str(mx), colr_code, colr_hex, 'e5e5e5', 2, 12)
            sld_file.write(rule)

        isLastRule = (i == (bincount))
        rule_mgl = create_mgl_rule(mi, colr_hex, layer_type, col_type, isLastRule)
        json_file.write(rule_mgl)

        mi = mx
        mx = (property_min + size) * (i+1)
    
    sld_file.write(footer_tpl)
    json_file.write(footer_tpl_mgl)
    sld_file.close()
    json_file.close()

def create_categorical_style_files(tablename, property_title, property_name, layer_type, col_type):
    cat_values_query = "select distinct(\"" + property_name + "\") from " + tablename
    cur.execute(cat_values_query)
    resultset = cur.fetchall()

    sld_filename = tablename + '_' + property_name + '.sld'
    json_filename = tablename + '_' + property_name + '.json'
    create_style_file_xml(tablename, property_name, sld_filename, json_filename)

    sld_file = open(sld_filename, 'w')
    json_file = open(json_filename, 'w')
    sld_file.write(header_tpl % (property_title))
    
    if layer_type == 'MULTIPOLYGON':
    	json_file.write(header_tpl_mgl % (tablename, tablename, 'fill', tablename))
        paint_prop = paint_tpl_fill_mgl % (property_name, 'categorical')
    elif layer_type == 'POINT' or layer_type == 'MULTIPOINT':
    	json_file.write(header_tpl_mgl % (tablename, tablename, 'circle', tablename))
        paint_prop = paint_tpl_circle_mgl % (property_name, 'categorical')
    elif layer_type == 'MULTILINESTRING':
      json_file.write(header_tpl_mgl % (tablename, tablename, 'line', tablename))
      paint_prop = paint_tpl_line_mgl % (property_name, 'categorical')
    json_file.write(paint_prop)

    i = 1
    nRules = len(resultset)

    for row in resultset:
        colr_code = 'c' + str(i)
        colr_hex = get_rand_color()
        
        if layer_type == 'MULTIPOLYGON':
            cat_rule = polygon_cat_rule_tpl % (row[0], row[0], property_name, row[0], colr_code, colr_hex, 'aaaaaa', 1)
            sld_file.write(cat_rule)
        elif layer_type == 'POINT':
            cat_rule = point_cat_rule_tpl % (row[0], row[0], property_name, row[0], colr_code, 'fd7569', 'e5e5e5', 2, 12)
            sld_file.write(cat_rule)

        isLastRule = (i == nRules);
        rule_mgl = create_mgl_rule(row[0], colr_hex, layer_type, col_type, isLastRule)
        json_file.write(rule_mgl)

        i = i + 1
        

    sld_file.write(footer_tpl)
    json_file.write(footer_tpl_mgl)
    sld_file.close()
    json_file.close()

def get_column_name(tablename, property_name):

    col_description_query = "select col_description((select oid from pg_class where relname = '" + tablename + "'), (select ordinal_position from information_schema.columns where table_name='" + tablename + "' and column_name='" + property_name + "'))"
    cur.execute(col_description_query)
    column_name = cur.fetchone()[0];

    return column_name


try:
    conn = psycopg2.connect("dbname='ibp' user='biodiv' host='localhost' password='biodiv'")
except:
    print "unable to connect to the database"

cur = conn.cursor()


#POINT
#tables = ['lyr_237_reconnaissance_soil','lyr_238_soilcorban99','lyr_239_soilcorban77','lyr_240_gudalur_mudumalai','lyr_241_wg_subbasin','lyr_242_wg_dam','lyr_243_wg_river','lyr_244_wg_watershed','lyr_245_wg_basin','lyr_246_kmtr','lyr_247_coimbatore_thrissur_floristictypes','lyr_248_coimbatore_thrissur_majorforest','lyr_249_coimbatore_thrissur_physiognomy','lyr_250_shimoga_majorforest','lyr_251_shimoga_physiognomy','lyr_252_shimoga_floristictypes','lyr_253_thiruvananthapuram_tirunelveli_physiognomy','lyr_254_thiruvananthapuram_tirunelveli_majorforest','lyr_255_thiruvananthapuram_tirunelveli_floristictypes','lyr_256_mercara_mysore_physiognomy','lyr_257_mercara_mysore_majorforest','lyr_258_mercara_mysore_floristictypes','lyr_259_belgaum_dharwar_panaji_floristictypes','lyr_260_belgaum_dharwar_panaji_majorforest','lyr_261_belgaum_dharwar_panaji_physiognomy','lyr_262_wg_simple_14class_vegetation']

#tables = ['lyr_289_location', 'lyr_290_distribution', 'lyr_291_kerala_rf_data']

#tables = ['lyr_319_forestdivisions', 'lyr_321_largecarnivores', 'lyr_323_rf_boundaries', 'lyr_325_primates', 'lyr_327_smallmammals', 'lyr_329_hornbill_nesting_wg', 'lyr_331_soi_toposheet_50k_index']

#tables = ['lyr_333_hornbill_nesting_wg']
#tables = ['lyr_336_kmtr_vegetation', 'lyr_338_medicinalplant', 'lyr_340_rocky_outcrop_location',  'lyr_342_ranwa_tree_location', 'lyr_344_wg_georegions_sing',  'lyr_346_chandoli_pa_location']

#tables = ['lyr_351_koyna_pa_location']

#tables = ['lyr_353_wg_whbsites']

#tables = ['lyr_355_rain_annual','lyr_357_jan_feb_rainfall','lyr_359_jun_sep_rainfall','lyr_361_westernghats_dams','lyr_363_mines_ingoa','lyr_365_cape_comorin','lyr_367_palani_hills','lyr_369_oct_dec_rain','lyr_371_march_may_rainfall','lyr_373_transect','lyr_375_hydro_electric_projects']

#tables = ['lyr_403_mammal_distribution']

#tables = ['lyr_405_nilgiri_wetland','lyr_408_plant_locations_aparna_watve','lyr_410_prachi_mehta_elephant','lyr_414_aquatic_plants','lyr_416_fish','lyr_418_india_georegions_sing','lyr_420_wti_elephant_corridor']

#tables = ['lyr_423_tourism_information']
#tables = ['lyr_429_amphibian_collections_wgrc','lyr_431_odonates','lyr_433_amphibians','lyr_435_brt_animal_sightings','lyr_437_brt_animal_sightings_part']
#tables = ['lyr_404_freshwater_kbas_kerala_tamilnadu']
#tables = ['lyr_117_india_soils']
#tables = ['lyr_410_butterflyspeciesdistribution']

tables = ['lyr_100_india_cpa', 'lyr_104_india_states_census01', 'lyr_105_india_districts', 'lyr_106_india_tahsils_census01', 'lyr_118_india_foresttypes', 'lyr_10_uttr_peaks', 'lyr_110_india_aquifer', 'lyr_112_india_geomorphology', 'lyr_115_india_tahsils', 'lyr_116_india_states', 'lyr_119_india_rainfallzone', 'lyr_117_india_soils', 'lyr_121_india_boundary', 'lyr_11_uttr_landtype', 'lyr_124_india_districts', 'lyr_12_uttr_trsite', 'lyr_13_uttr_towns', 'lyr_14_uttr_bugyal', 'lyr_156_india_biogeographic', 'lyr_162_india_temperature', 'lyr_157_india_ecoregionwwf200', 'lyr_158_india_geology', 'lyr_159_india_physiography', 'lyr_15_uttr_state', 'lyr_160_brt_vegetation', 'lyr_163_india_ecoregionscse', 'lyr_161_india_riverbasins', 'lyr_167_brt_vegplots', 'lyr_164_india_agroeco', 'lyr_168_india_achatina', 'lyr_16_uttr_pa', 'lyr_171_india_landmarktrees', 'lyr_171_india_tigerreserves', 'lyr_177_kanakapura_schools', 'lyr_200_dryseason', 'lyr_17_uttr_vbound', 'lyr_183_eh_cepf_grants', 'lyr_185_wg_cepf_grants', 'lyr_18_papagni_lusecover', 'lyr_19_papagni_dnnet', 'lyr_1_wg_cepflandcorr', 'lyr_202_rainfall', 'lyr_206_temperature', 'lyr_208_wg_mapbound', 'lyr_20_papagni_sub_watersheds', 'lyr_210_india_checklists', 'lyr_218_wg_fire_2005', 'lyr_212_wg_fire_2002', 'lyr_213_wg_fire_2003', 'lyr_214_wg_fire_2010', 'lyr_21_papagni_geology', 'lyr_215_wg_fire_2007', 'lyr_216_wg_fire_2004', 'lyr_217_wg_fire_2008', 'lyr_219_wg_fire_2006', 'lyr_220_wg_fire_2001', 'lyr_221_wg_fire_2000', 'lyr_222_wg_fire_2009', 'lyr_22_papagni_forest', 'lyr_241_belgaum_dharwar_panaji_majorforest', 'lyr_235_wg_birdtransects', 'lyr_237_reconnaissance_soil', 'lyr_238_western_anamalai', 'lyr_239_belgaum_dharwar_panaji_floristictypes', 'lyr_23_papagni_boundary', 'lyr_242_kmtr', 'lyr_240_belgaum_dharwar_panaji_physiognomy', 'lyr_244_mercara_mysore_floristictypes', 'lyr_243_mercara_mysore_physiognomy', 'lyr_245_mercara_mysore_majorforest', 'lyr_246_thiruvananthapuram_tirunelveli_floristictypes', 'lyr_247_thiruvananthapuram_tirunelveli_physiognomy', 'lyr_248_thiruvananthapuram_tirunelveli_majorforest', 'lyr_249_wg_simple_14class_vegetation', 'lyr_255_shimoga_physiognomy', 'lyr_24_papagni_waterbody', 'lyr_250_coimbatore_thrissur_majorforest', 'lyr_251_coimbatore_thrissur_physiognomy', 'lyr_252_coimbatore_thrissur_floristictypes', 'lyr_253_shimoga_majorforest', 'lyr_256_soilcorban77', 'lyr_254_shimoga_floristictypes', 'lyr_258_gudalur_mudumalai', 'lyr_257_soilcorban99', 'lyr_259_wg_subbasin', 'lyr_25_papagni_habpt', 'lyr_260_wg_watershed', 'lyr_293_kerala_rf_data', 'lyr_261_wg_river', 'lyr_262_wg_dam', 'lyr_299_wg_bats', 'lyr_263_wg_basin', 'lyr_26_papagni_vill_pca', 'lyr_27_papagni_rainfall', 'lyr_294_distribution', 'lyr_28_papagni_landscape', 'lyr_291_coimbatore_thrissur_physiognomy', 'lyr_295_location', 'lyr_29_papagni_micro_watersheds', 'lyr_2_wg_endemicsspsatlas', 'lyr_301_wg_bird_tree_transcet', 'lyr_303_primates', 'lyr_304_smallmammals', 'lyr_322_chandoli_pa_location', 'lyr_305_forestdivisions', 'lyr_306_rf_boundaries', 'lyr_308_soi_toposheet_50k_index', 'lyr_320_wg_georegions_sing', 'lyr_309_largecarnivores', 'lyr_30_ne_cepfpriorsites', 'lyr_317_hornbill_nesting_wg', 'lyr_31_ne_cepfpriorcorridors', 'lyr_321_kmtr_vegetation', 'lyr_323_medicinalplant', 'lyr_324_ranwa_tree_location', 'lyr_325_rocky_outcrop_location', 'lyr_32_nbr_vegtype', 'lyr_332_bangalore_roads', 'lyr_34_brt_boundary', 'lyr_334_koyna_pa_location', 'lyr_336_wg_whbsites', 'lyr_338_cape_comorin', 'lyr_346_jun_sep_rainfall', 'lyr_33_nbr_vegsample', 'lyr_340_transect', 'lyr_342_jan_feb_rainfall', 'lyr_344_march_may_rainfall', 'lyr_348_oct_dec_rain', 'lyr_350_hydro_electric_projects', 'lyr_352_palani_hills', 'lyr_354_westernghats_dams', 'lyr_356_mines_ingoa', 'lyr_358_rain_annual', 'lyr_370_aquatic_plants', 'lyr_35_brt_fire2001', 'lyr_360_birdingplacesbangalore_points', 'lyr_362_birdingplacesbangalore_polygons', 'lyr_369_wti_elephant_corridor', 'lyr_364_mammal_distribution', 'lyr_366_fish', 'lyr_367_india_georegions_sing', 'lyr_368_nilgiri_wetland', 'lyr_36_brt_fire2000', 'lyr_371_plant_locations_aparna_watve', 'lyr_372_prachi_mehta_elephant', 'lyr_37_brt_biomasschangepattern1973_98', 'lyr_381_tourism_information', 'lyr_383_amphibian_collections_wgrc', 'lyr_39_brt_biomasschange1973_98', 'lyr_385_amphibians', 'lyr_387_brt_animal_sightings', 'lyr_389_brt_animal_sightings_part', 'lyr_397_uttarkannada_ltm', 'lyr_38_brt_fire2007', 'lyr_391_odonates', 'lyr_393_soi_topo_india_250k', 'lyr_395_soi_topo_india_50k', 'lyr_399_uttarkannada_pa', 'lyr_3_wg_flow', 'lyr_401_fishbasegbifindiadistribution', 'lyr_403_freshwater_kbas_kerala_tamilnadu', 'lyr_404_freshwater_kbas_kerala_tamilnadu', 'lyr_405_freshwater_kba_focal_areas_kerala_tamil_nadu', 'lyr_406_grassland_1900m', 'lyr_407_achatina_fulica', 'lyr_408_indrella_ampulla', 'lyr_409_nasikabatrachus', 'lyr_410_butterflyspeciesdistribution', 'lyr_42_brt_ranges', 'lyr_41_brt_lantanadistribution', 'lyr_43_brt_fire2005', 'lyr_44_brt_fire1999', 'lyr_45_brt_fire2003', 'lyr_46_brt_watershed', 'lyr_47_brt_villages', 'lyr_48_vembanad_birdsurvey', 'lyr_54_bichhia_geology', 'lyr_49_vembanad_lake', 'lyr_4_wg_cepfpriorsites', 'lyr_50_vembanad_basinstations', 'lyr_51_pench_alldholes_mcps', 'lyr_52_pench_alldholes_geog', 'lyr_55_bichhia_drain', 'lyr_53_bichhia_soil', 'lyr_57_bichhia_habpts', 'lyr_56_bichhia_pfrf', 'lyr_58_bichhia_luse', 'lyr_59_bichhia_watershed', 'lyr_5_wg_cepfcrtlinks', 'lyr_60_bandipur_villages', 'lyr_61_bandipur_apcwtfrh', 'lyr_68_agar_soil', 'lyr_62_bandipurtanks_polygon', 'lyr_63_bandipur_boundary', 'lyr_64_bandipurtanks_point', 'lyr_65_satkoshia_vegplot1', 'lyr_66_satkoshia_vegplots2', 'lyr_69_agar_wells', 'lyr_67_agar_geology', 'lyr_70_agar_habpts', 'lyr_6_wg_mammals_ncf', 'lyr_71_agar_watershed', 'lyr_74_india_whbpoints', 'lyr_76_india_imdstations', 'lyr_78_india_birdlocations', 'lyr_7_wg_boundary', 'lyr_92_india_ramsarsites', 'lyr_83_india_habitats', 'lyr_84_india_cheetah', 'lyr_85_india_papoints', 'lyr_86_india_ciap', 'lyr_89_india_sandbox', 'lyr_9_uttr_habpts', 'lyr_8_wg_flow_catchments']

cont_type = ['bigint', 'integer', 'numeric', 'smallint', 'double precision', 'real']     

for tablename in tables:
    print "layer: " + tablename
    meta_layer_query = 'select color_by, layer_type from "Meta_Layer" where layer_tablename=\'' + tablename + '\''
    cur.execute(meta_layer_query)
    layer_type = cur.fetchone()[1].strip("'"); # MULTIPOLYGON / POINT / MULTILINESTRING / MULTIPOINT

    colname_datatype_query = "select column_name, data_type from information_schema.columns where table_name = '" + tablename + "'"
    cur.execute(colname_datatype_query)
    resultset = cur.fetchall()
    for row in resultset:
        col_type = row[1];
        if not row[0].startswith('__mlocate__') and col_type.startswith('character'):
            column_name = get_column_name(tablename, row[0])
            create_categorical_style_files(tablename, column_name, row[0], layer_type, col_type)

        elif not row[0].startswith('__mlocate__') and cont_type.count(col_type) == 1:
            column_name = get_column_name(tablename, row[0])
            min_max_query = 'SELECT  min(' + row[0] + '), max(' + row[0] + ') FROM ' + tablename
            cur.execute(min_max_query)
            min_max_resultset = cur.fetchall()
            for minimum, maximum in min_max_resultset:
                if minimum != None and maximum != None:
                    create_style_files(tablename, column_name, row[0], minimum, maximum, 5, layer_type, col_type)

os.system("mkdir styles")
os.system("mv *.xml *.sld *.json styles")
