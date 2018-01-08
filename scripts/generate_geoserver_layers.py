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

def get_keywords_xml(tablename):
    
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
    cont_type = ['bigint', 'integer', 'numeric', 'smallint', 'double precision', 'real']

    styles = ''
    colname_datatype_query = "select column_name, data_type from information_schema.columns where table_name = '" + tablename + "'"
    cur.execute(colname_datatype_query)
    resultset = cur.fetchall()
    for row in resultset:
        if not row[0].startswith('__mlocate__') and (row[1].startswith('character') or (cont_type.count(row[1]) == 1)):
            styles = styles + '<style>\n  <id>' + tablename + '_' + row[0] + '</id>\n</style>'

    return styles            

def create_layer_xml(tablename):

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

try:
    conn = psycopg2.connect("dbname='ibp' user='biodiv' host='localhost' password='biodiv'")
except:
    print "unable to connect to the database"

cur = conn.cursor()



#tables = ['lyr_237_reconnaissance_soil','lyr_238_soilcorban99','lyr_239_soilcorban77','lyr_240_gudalur_mudumalai','lyr_241_wg_subbasin','lyr_242_wg_dam','lyr_243_wg_river','lyr_244_wg_watershed','lyr_245_wg_basin','lyr_246_kmtr','lyr_247_coimbatore_thrissur_floristictypes','lyr_248_coimbatore_thrissur_majorforest','lyr_249_coimbatore_thrissur_physiognomy','lyr_250_shimoga_majorforest','lyr_251_shimoga_physiognomy','lyr_252_shimoga_floristictypes','lyr_253_thiruvananthapuram_tirunelveli_physiognomy','lyr_254_thiruvananthapuram_tirunelveli_majorforest','lyr_255_thiruvananthapuram_tirunelveli_floristictypes','lyr_256_mercara_mysore_physiognomy','lyr_257_mercara_mysore_majorforest','lyr_258_mercara_mysore_floristictypes','lyr_259_belgaum_dharwar_panaji_floristictypes','lyr_260_belgaum_dharwar_panaji_majorforest','lyr_261_belgaum_dharwar_panaji_physiognomy','lyr_262_wg_simple_14class_vegetation']

#tables = ['lyr_202_rainfall','lyr_200_dryseason','lyr_206_temperature','lyr_171_india_tigerreserves','lyr_69_agar_wells','lyr_220_wg_fire_2001','lyr_177_kanakapura_schools','lyr_221_wg_fire_2000','lyr_222_wg_fire_2009','lyr_3_wg_flow','lyr_48_vembanad_birdsurvey','lyr_50_vembanad_basinstations','lyr_33_nbr_vegsample','lyr_83_india_habitats','lyr_212_wg_fire_2002','lyr_213_wg_fire_2003','lyr_214_wg_fire_2010','lyr_85_india_papoints','lyr_52_pench_alldholes_geog','lyr_9_uttr_habpts','lyr_215_wg_fire_2007','lyr_216_wg_fire_2004','lyr_217_wg_fire_2008','lyr_183_eh_cepf_grants','lyr_185_wg_cepf_grants','lyr_218_wg_fire_2005','lyr_74_india_whbpoints','lyr_219_wg_fire_2006']

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
#tables = ['lyr_412_nasikabatrachus']
#tables = ['lyr_177_kanakapura_schools']
tables = ['lyr_100_india_cpa', 'lyr_104_india_states_census01', 'lyr_105_india_districts', 'lyr_106_india_tahsils_census01', 'lyr_118_india_foresttypes', 'lyr_10_uttr_peaks', 'lyr_110_india_aquifer', 'lyr_112_india_geomorphology', 'lyr_115_india_tahsils', 'lyr_116_india_states', 'lyr_119_india_rainfallzone', 'lyr_117_india_soils', 'lyr_121_india_boundary', 'lyr_11_uttr_landtype', 'lyr_124_india_districts', 'lyr_12_uttr_trsite', 'lyr_13_uttr_towns', 'lyr_14_uttr_bugyal', 'lyr_156_india_biogeographic', 'lyr_162_india_temperature', 'lyr_157_india_ecoregionwwf200', 'lyr_158_india_geology', 'lyr_159_india_physiography', 'lyr_15_uttr_state', 'lyr_160_brt_vegetation', 'lyr_163_india_ecoregionscse', 'lyr_161_india_riverbasins', 'lyr_167_brt_vegplots', 'lyr_164_india_agroeco', 'lyr_168_india_achatina', 'lyr_16_uttr_pa', 'lyr_171_india_landmarktrees', 'lyr_171_india_tigerreserves', 'lyr_177_kanakapura_schools', 'lyr_200_dryseason', 'lyr_17_uttr_vbound', 'lyr_183_eh_cepf_grants', 'lyr_185_wg_cepf_grants', 'lyr_18_papagni_lusecover', 'lyr_19_papagni_dnnet', 'lyr_1_wg_cepflandcorr', 'lyr_202_rainfall', 'lyr_206_temperature', 'lyr_208_wg_mapbound', 'lyr_20_papagni_sub_watersheds', 'lyr_210_india_checklists', 'lyr_218_wg_fire_2005', 'lyr_212_wg_fire_2002', 'lyr_213_wg_fire_2003', 'lyr_214_wg_fire_2010', 'lyr_21_papagni_geology', 'lyr_215_wg_fire_2007', 'lyr_216_wg_fire_2004', 'lyr_217_wg_fire_2008', 'lyr_219_wg_fire_2006', 'lyr_220_wg_fire_2001', 'lyr_221_wg_fire_2000', 'lyr_222_wg_fire_2009', 'lyr_22_papagni_forest', 'lyr_241_belgaum_dharwar_panaji_majorforest', 'lyr_235_wg_birdtransects', 'lyr_237_reconnaissance_soil', 'lyr_238_western_anamalai', 'lyr_239_belgaum_dharwar_panaji_floristictypes', 'lyr_23_papagni_boundary', 'lyr_242_kmtr', 'lyr_240_belgaum_dharwar_panaji_physiognomy', 'lyr_244_mercara_mysore_floristictypes', 'lyr_243_mercara_mysore_physiognomy', 'lyr_245_mercara_mysore_majorforest', 'lyr_246_thiruvananthapuram_tirunelveli_floristictypes', 'lyr_247_thiruvananthapuram_tirunelveli_physiognomy', 'lyr_248_thiruvananthapuram_tirunelveli_majorforest', 'lyr_249_wg_simple_14class_vegetation', 'lyr_255_shimoga_physiognomy', 'lyr_24_papagni_waterbody', 'lyr_250_coimbatore_thrissur_majorforest', 'lyr_251_coimbatore_thrissur_physiognomy', 'lyr_252_coimbatore_thrissur_floristictypes', 'lyr_253_shimoga_majorforest', 'lyr_256_soilcorban77', 'lyr_254_shimoga_floristictypes', 'lyr_258_gudalur_mudumalai', 'lyr_257_soilcorban99', 'lyr_259_wg_subbasin', 'lyr_25_papagni_habpt', 'lyr_260_wg_watershed', 'lyr_293_kerala_rf_data', 'lyr_261_wg_river', 'lyr_262_wg_dam', 'lyr_299_wg_bats', 'lyr_263_wg_basin', 'lyr_26_papagni_vill_pca', 'lyr_27_papagni_rainfall', 'lyr_294_distribution', 'lyr_28_papagni_landscape', 'lyr_291_coimbatore_thrissur_physiognomy', 'lyr_295_location', 'lyr_29_papagni_micro_watersheds', 'lyr_2_wg_endemicsspsatlas', 'lyr_301_wg_bird_tree_transcet', 'lyr_303_primates', 'lyr_304_smallmammals', 'lyr_322_chandoli_pa_location', 'lyr_305_forestdivisions', 'lyr_306_rf_boundaries', 'lyr_308_soi_toposheet_50k_index', 'lyr_320_wg_georegions_sing', 'lyr_309_largecarnivores', 'lyr_30_ne_cepfpriorsites', 'lyr_317_hornbill_nesting_wg', 'lyr_31_ne_cepfpriorcorridors', 'lyr_321_kmtr_vegetation', 'lyr_323_medicinalplant', 'lyr_324_ranwa_tree_location', 'lyr_325_rocky_outcrop_location', 'lyr_32_nbr_vegtype', 'lyr_332_bangalore_roads', 'lyr_34_brt_boundary', 'lyr_334_koyna_pa_location', 'lyr_336_wg_whbsites', 'lyr_338_cape_comorin', 'lyr_346_jun_sep_rainfall', 'lyr_33_nbr_vegsample', 'lyr_340_transect', 'lyr_342_jan_feb_rainfall', 'lyr_344_march_may_rainfall', 'lyr_348_oct_dec_rain', 'lyr_350_hydro_electric_projects', 'lyr_352_palani_hills', 'lyr_354_westernghats_dams', 'lyr_356_mines_ingoa', 'lyr_358_rain_annual', 'lyr_370_aquatic_plants', 'lyr_35_brt_fire2001', 'lyr_360_birdingplacesbangalore_points', 'lyr_362_birdingplacesbangalore_polygons', 'lyr_369_wti_elephant_corridor', 'lyr_364_mammal_distribution', 'lyr_366_fish', 'lyr_367_india_georegions_sing', 'lyr_368_nilgiri_wetland', 'lyr_36_brt_fire2000', 'lyr_371_plant_locations_aparna_watve', 'lyr_372_prachi_mehta_elephant', 'lyr_37_brt_biomasschangepattern1973_98', 'lyr_381_tourism_information', 'lyr_383_amphibian_collections_wgrc', 'lyr_39_brt_biomasschange1973_98', 'lyr_385_amphibians', 'lyr_387_brt_animal_sightings', 'lyr_389_brt_animal_sightings_part', 'lyr_397_uttarkannada_ltm', 'lyr_38_brt_fire2007', 'lyr_391_odonates', 'lyr_393_soi_topo_india_250k', 'lyr_395_soi_topo_india_50k', 'lyr_399_uttarkannada_pa', 'lyr_3_wg_flow', 'lyr_401_fishbasegbifindiadistribution', 'lyr_403_freshwater_kbas_kerala_tamilnadu', 'lyr_404_freshwater_kbas_kerala_tamilnadu', 'lyr_405_freshwater_kba_focal_areas_kerala_tamil_nadu', 'lyr_406_grassland_1900m', 'lyr_407_achatina_fulica', 'lyr_408_indrella_ampulla', 'lyr_409_nasikabatrachus', 'lyr_410_butterflyspeciesdistribution', 'lyr_42_brt_ranges', 'lyr_41_brt_lantanadistribution', 'lyr_43_brt_fire2005', 'lyr_44_brt_fire1999', 'lyr_45_brt_fire2003', 'lyr_46_brt_watershed', 'lyr_47_brt_villages', 'lyr_48_vembanad_birdsurvey', 'lyr_54_bichhia_geology', 'lyr_49_vembanad_lake', 'lyr_4_wg_cepfpriorsites', 'lyr_50_vembanad_basinstations', 'lyr_51_pench_alldholes_mcps', 'lyr_52_pench_alldholes_geog', 'lyr_55_bichhia_drain', 'lyr_53_bichhia_soil', 'lyr_57_bichhia_habpts', 'lyr_56_bichhia_pfrf', 'lyr_58_bichhia_luse', 'lyr_59_bichhia_watershed', 'lyr_5_wg_cepfcrtlinks', 'lyr_60_bandipur_villages', 'lyr_61_bandipur_apcwtfrh', 'lyr_68_agar_soil', 'lyr_62_bandipurtanks_polygon', 'lyr_63_bandipur_boundary', 'lyr_64_bandipurtanks_point', 'lyr_65_satkoshia_vegplot1', 'lyr_66_satkoshia_vegplots2', 'lyr_69_agar_wells', 'lyr_67_agar_geology', 'lyr_70_agar_habpts', 'lyr_6_wg_mammals_ncf', 'lyr_71_agar_watershed', 'lyr_74_india_whbpoints', 'lyr_76_india_imdstations', 'lyr_78_india_birdlocations', 'lyr_7_wg_boundary', 'lyr_92_india_ramsarsites', 'lyr_83_india_habitats', 'lyr_84_india_cheetah', 'lyr_85_india_papoints', 'lyr_86_india_ciap', 'lyr_89_india_sandbox', 'lyr_9_uttr_habpts', 'lyr_8_wg_flow_catchments']

for tablename in tables:
    print 'layer: ' + tablename
    os.system("mkdir " + "layers/" + tablename)
    create_featuretype_xml(tablename)
    create_layer_xml(tablename)
    os.system("mv layer.xml featuretype.xml " + "layers/" + tablename)
