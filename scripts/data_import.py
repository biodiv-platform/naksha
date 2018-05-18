import os,sys,shutil
if(len(sys.argv) not in [4,5]):
        print "Expected args 4 or 5, Got args "+str(len(sys.argv) -1)
        print "Usage: python data_import DBNAME DBUSER DATA_PATH [GEO_CITY]";
        sys.exit(1)

geoserver_data_dir_path = '/apps/biodiv/geoserver_data_dir_new'
dbname = sys.argv[1]
dbuser = sys.argv[2]
datapath = sys.argv[3]
theme = ''
if(len(sys.argv) == 5):
        theme = sys.argv[4]
def import_layers(dbname, dbuser, pth, sql_file_name):
    global DBNAME, DBUSER
    DBNAME = dbname
    DBUSER = dbuser
    global theDictStr, cursr, GEOMCOL, shp2pgsql, psql, cwdir, err, state, g_layer_tablename, g_link_tablename, columnnamekeys, layer_seq, link_seq
    theDictStr = ""
    cursr = 0
    GEOMCOL = "__mlocate__topology"
    shp2pgsql = "shp2pgsql"
    psql = "psql"
    cwdir = os.getcwd()
    err = 0
    state = 0
    g_layer_tablename = ""
    g_link_tablenames = {}
    
    columnnamekeys = [
      'summary_columns',
      'filter_columns',
      'search_columns',
      'editable_columns',
      'title_column',
      'color_by',
      'size_by',
      'linked_column',
      'layer_column',
      'resource_column',
      'table_column',
      'media_columns',
      #'url_columns',
      'italics_columns',
      'mandatory_columns',
      'record_type_column'
    ]
    global ROLE_TYPES, ROLE_PERMISSIONS
    ROLE_TYPES = [
      'admin',
      'validator',
      'member'
    ]
    
    ROLE_PERSMISSIONS = {
      'admin' : '''create node_mlocate, delete any node_mlocate, delete own node_mlocate, edit any node_mlocate, edit own node_mlocate, create node_mlocate_layerinfo, delete any node_mlocate_layerinfo, 
        delete own node_mlocate_layerinfo, edit any node_mlocate_layerinfo, edit own node_mlocate_layerinfo, access node_mlocate_participation, create node_mlocate_participation, delete any node_mlocate_participation, delete own node_mlocate_participation, edit any node_mlocate_participation, edit own node_mlocate_participation''',
      'validator' : 'create node_mlocate, delete own node_mlocate, edit own node_mlocate, create node_mlocate_layerinfo, delete own node_mlocate_layerinfo, edit own node_mlocate_layerinfo',
      'member' : 'create node_mlocate, delete own node_mlocate, edit own node_mlocate, create node_mlocate_layerinfo, delete own node_mlocate_layerinfo, edit own node_mlocate_layerinfo'
    }
    #######################################
    # Allowed columns in link table file. #
    #######################################
    ALLOWED_COLUMNS = ['INTEGER','TEXT','DECIMAL','DATE']
    
    
    #################################################
    # SQL columns for corresponding allowed columns #
    #################################################
    SQL_COLUMNS = {'INTEGER': 'INTEGER',
                   'TEXT': 'VARCHAR(1024)',
                   'DECIMAL': 'float(24)',
                   'DATE': 'DATE'}
    
    #########################################
    # Validate if we get an unknown column. #
    #########################################
    def validate_column_types(types):
        for t in types:
            if t not in ALLOWED_COLUMNS:
                print "Unknown column:", t
                sys.exit(2)
        pass
    
    
    ##############################################################
    # Validate column names, see if they contain reserved words  #
    # like "year", "count", or contain spaces.                   #
    # TODO                                                       #
    ##############################################################
    def validate_column_names(names):
        NOT_ALLOWED_CHARS = [' ', '\t']
        pass
    
    def convert_to_utf8(filename):
        # gather the encodings you think that the file may be
        # encoded inside a tuple
        encodings = ('windows-1253', 'iso-8859-7', 'macgreek')
    
        # try to open the file and exit if some IOError occurs
        try:
            f = open(filename, 'r').read()
        except Exception:
            sys.exit(1)
    
        # now start iterating in our encodings tuple and try to
        # decode the file
        for enc in encodings:
            try:
                # try to decode the file with the first encoding
                # from the tuple.
                # if it succeeds then it will reach break, so we
                # will be out of the loop (something we want on
                # success).
                # the data variable will hold our decoded text
                data = f.decode(enc)
                break
            except Exception:
                # if the first encoding fail, then with the continue
                # keyword will start again with the second encoding
                # from the tuple an so on.... until it succeeds.
                # if for some reason it reaches the last encoding of
                # our tuple without success, then exit the program.
                if enc == encodings[-1]:
                    sys.exit(1)
                continue
    
        # now get the absolute path of our filename and append .bak
        # to the end of it (for our backup file)
        fpath = os.path.abspath(filename)
        newfilename = fpath + '.bak'
        # and make our backup file with shutil
        shutil.copy(filename, newfilename)
    
        # and at last convert it to utf-8
        f = open(filename, 'w')
        try:
            f.write(data.encode('utf-8'))
        except Exception, e:
            print e
        finally:
            f.close()
    
    def validate_textfile_tabdata(fname, flines):
      global err
      chk_len = len(flines[0].replace("\n", "").split('\t'))
    
      taberr = 0
      errlog = ""
      flen = len(flines)
      for i in range(1,flen):
        curlen = len(flines[i].replace("\n", "").split('\t'))
        if not chk_len == curlen:
          if(taberr == 0):
            err = 1
            errlog = "Expecting %s tabs on each line\n" % (chk_len-1)
            taberr = 1
          errlog += "Line no: %s; Tabs: %s\n" % (i+1, curlen-1)
    
      if(taberr == 1):
        fname = os.path.join("textdata_issues", fname.replace("\\", "__"))
        f = open(fname, "w")
        f.write(errlog)
        f.close()
      return taberr
    
    def gen_LinkTable_Sql(link_tablename, linkTable_filename):
        filename = linkTable_filename
        print '@REM # -- Reading link table: "%s"' % filename
        # Read linked table file.
        input_lines = open(filename).readlines()
    
        #################################################
        #Instead of reporting error and exiting, append null values if number of tabs are less
        #################################################
        #if(validate_textfile_tabdata(filename, input_lines)):
        #  return "", ""
    
        # Read column names specified in linked table.
        specified_column_names = input_lines[0].strip("\n\r\t").split("\t")
        #v#specified_column_names.append('created_by')
        #v#specified_column_names.append('modified_by')
        #v#specified_column_names.append('creation_date')
        #v#specified_column_names.append('modified_date')
        specified_column_names = map(lambda x: '"' + x.lower() + '"', specified_column_names)
        table_column_names = input_lines[0].strip("\n\r\t").split("\t")
        table_column_names = map(lambda x: x.lower(), table_column_names)
        # Additional columns.
        #v#table_column_names.append('created_by')
        #v#table_column_names.append('modified_by')
        #v#table_column_names.append('creation_date')
        #v#table_column_names.append('modification_date')
        validate_column_names(table_column_names)
    
        # Read column types specified in linked table.
        specified_column_types = input_lines[1].strip("\n\r\t").split("\t")
        table_column_types = input_lines[1].strip("\n\r\t").split("\t")
        # Additional column types.
        #v#table_column_types.append('NUMBER')
        #v#table_column_types.append('NUMBER')
        #v#table_column_types.append('DATE')
        #v#table_column_types.append('DATE')
        validate_column_types(table_column_types)
    
        # The table name to load the data.
        #table_name = filename.split("/")[-1].split(".")[0]
        table_name = link_tablename
    
        if len(table_column_types) != len(table_column_names):
            print "Can not read meta data.", table_column_names, table_column_types
    
        # Generate Table field definitions.
        i = 0
        field_defs = """"""
        while i < len(table_column_names) - 1:
            field_defs = field_defs + ('  "%s"\t%s,\n' % ( table_column_names[i], SQL_COLUMNS[table_column_types[i]]))
            i = i + 1
        field_defs = field_defs + ('  "%s"\t%s' % ( table_column_names[i], SQL_COLUMNS[table_column_types[i]]))
    
    
        # Start writing the SQL file.
        # Code to connect to database and create the table.
        x = """
    --  Create the table
    CREATE TABLE "%s" (
      PRIMARY KEY (__mlocate__id),
    %s
    )
    INHERITS ("Link_Template");
    """ % (table_name, field_defs)
    
    
        insertsql = ""
        # Code to generate insert statements to load the data.
        chk_len = len(specified_column_names)
        for data_line in input_lines[2:]:
            data_line = data_line.strip("\n\r")
            vals = []
            data_arr = data_line.split("\t", chk_len-1)
            for i in range(0, len(data_arr)):
                if(data_arr[i].strip() == ''):
                    vals.append("NULL")
                elif specified_column_types[i] == "INTEGER" or specified_column_types[i] == "DECIMAL":
                    vals.append("%s" % data_arr[i])
                else:
                    vals.append("'%s'" % data_arr[i].replace("'","''"))
    
            # append NULL values when number of tabs are less in a row
            len_diff = (chk_len - len(vals))
            if len_diff:
                vals.extend(["NULL"]*len_diff)
    
            y = """INSERT INTO "%s"(%s) VALUES (%s);
    """ % ( table_name, ",".join(specified_column_names), ",".join( vals ))
    #""" % ( table_name, ",".join(specified_column_names), ",".join( [ "'%s'" % k.replace("'","''") for k in data_line.split("\t", chk_len)] ))
    
            #x = x + y
            insertsql += y
    
        # Code to set created by and modified_by variables.
        z = """
    UPDATE "%s" set __mlocate__created_by = '1';
    UPDATE "%s" set __mlocate__modified_by = '1';
    
    UPDATE "%s" set __mlocate__created_date = now();
    UPDATE "%s" set __mlocate__modified_date = now();
    """ % (table_name, table_name, table_name, table_name)
    
        #x = x + z
    
        # Write everything to a file.
        #output = open(outputfilename, "w").write(x)
        return x, insertsql+z
    
    def runCmd(cmmd):
        import popen2
    
        r, w, e = popen2.popen3(cmmd)
        a = e.readlines()
        b = r.readlines()
        r.close()
        e.close()
        try:
            w.close()
        except:
            pass
        return a,b
    
    def encodeUTF8(str1):
      print str1
      return unicode(str1, "utf-8")
    
    def insertSql(fln1, sql, sql2):
      fln2 = fln1+"_tmp"
      #import codecs
      #f1 = codecs.open( fln1, "r", "utf-8" )
      f1 = open(fln1)
      f2 = open(fln2, "w")
      fl1 = f1.read()
      fl1 = fl1.replace("END;\nBEGIN;\n", "")
      f2.write(fl1)
      f1.close()
      f2.close()
      shutil.move(fln2, fln1)
    
      f1 = open(fln1)
      f2 = open(fln2, "w")
    
      i = 0;
      j = 0;
      for line in f1:
        if(i == 1):
          f2.write(sql+"\n")
        if(line.startswith("CREATE INDEX")):
          f2.write("\n")
        #if(j == 0 and line.startswith("END")):
        if(j == 0 and line.startswith("COMMIT")):
          f2.write(sql2)
          j += 1
        #f2.write(encodeUTF8(line))
        f2.write(line)
        if i == 0:
          f2.write("set client_encoding = Latin1;\n")
          #f2.write("set datestyle = European;\n")
        i += 1
    
      f1.close()
      f2.close()
    
      shutil.move(fln2, fln1)
    
    # def get_fields(dbfname):
      # if dbfname.strip() == "":
        # return {}
      # from dbfpy import dbf
      # fields = {}
      # dbf1 = dbf.Dbf(dbfname)
      # for field in dbf1.fieldDefs:
        # fields[str(field.name)] = str(field.typeCode)
      # return fields
    
    def escapeStr(str1):
      str1 = str1.strip()
      str1 = str1.replace('"', '\"')
      str1 = str1.replace('\\', '\\\\')
      return str1
    
    def getkeyvalues(has_rows, indnt, pth):
      global theDictStr, fl_lines, fl_len, cursr, err, columnnamekeys
      global layer_seq, link_seq, g_layer_tablename, g_link_tablenames
      i = 0
      indnt = indnt + '  '
      spc = ''
    
      if has_rows:
        spc = '  '
      if has_rows:
        theDictStr += indnt + '"' + str(i) + '" : {\n'
      while cursr < fl_len:
        line = fl_lines[cursr].strip()
        if line.startswith('*'):
          if has_rows:
            theDictStr += indnt + '},\n'
          cursr -= 1
          return "*"
        elif line == '#':
          if has_rows:
            theDictStr += indnt + '},\n'
            i += 1
            theDictStr += indnt + '"' + str(i) + '" : {\n'
            cursr += 1
            continue
          else:
            theDictStr += indnt + '},\n'
            cursr += 1
            return "#"
        elif line == '':
          cursr += 1
          continue
        elif line == '$Layer_Column_Description':
          cursr += 1
          Layer_Column_Description('  ', pth)
          continue
        elif line == '$LinkTable_Column_Description':
          cursr += 1
          ret = LinkTable_Column_Description('  ', pth)
          if has_rows and ret == "#":
            i += 1
            theDictStr += indnt + '"' + str(i) + '" : {\n'
          continue
        else:
          key, sep, val = line.partition(':')
          key = key.strip().replace("\t", " ")
          if(key.find(" ") != -1):
            err = 1
            theDictStr = ""
            print '@REM #Error in key "%s"' % key
            return -1
          if key == 'link_tablename':
            #theDictStr += indnt + spc + '"' + escapeStr(key) + '" : "' + escapeStr(val.replace(".dbf", "").replace(".txt", "")) + '",\n'
            tablename = "lnk_" + str(link_seq) + "_" + escapeStr(val).replace(".dbf", "").replace(".txt", "")
            theDictStr += indnt + spc + '"' + escapeStr(key) + '" : "' + tablename + '",\n'
            g_link_tablenames[escapeStr(val).replace(".dbf", "").replace(".txt", "")] = tablename
            link_seq += 1
            theDictStr += indnt + spc + '"linkTable_filename" : "' + escapeStr(os.path.join(pth, "linktable", escapeStr(val).replace(".dbf", "").replace(".txt", "") + ".txt")) + '",\n'
          elif key == 'layer_tablename':
            g_layer_tablename = "lyr_" + str(layer_seq) + "_" + escapeStr(val).replace(".dbf", "").replace(".txt", "")
            #theDictStr += indnt + spc + '"' + escapeStr(key) + '" : "' + escapeStr(val.replace(".dbf", "").replace(".txt", "")) + '",\n'
            theDictStr += indnt + spc + '"' + escapeStr(key) + '" : "' + g_layer_tablename + '",\n'
            layer_seq += 1
          elif key in columnnamekeys:
            val1 = sanitizecolumnnames(g_layer_tablename, escapeStr(val.lower()))
            theDictStr += indnt + spc + '"' + escapeStr(key) + '" : "' + val1 + '",\n'
          elif key == 'FID' or key == 'SHAPE':
            pass
          else:
            theDictStr += indnt + spc + '"' + escapeStr(key) + '" : "' + escapeStr(val) + '",\n'
          cursr += 1
      if has_rows:
        theDictStr += indnt + '},\n'
    
    def sanitizecolumnnames(tablename, cols):
      cols = cols.strip(" \n")
      cols_lst = cols.split(",")
      new_cols = ""
      for col in cols_lst:
        col = col.strip(" \r\n")
        if col != "":
          if not col.startswith("'"):
            col = "'" + col
            print """@REM Error: %s: "'" missing in column name at start: %s""" % (tablename, col)
          if not col.endswith("'"):
            col = col + "'"
            print """@REM Error: %s: "'" missing in column name at end: %s""" % (tablename, col)
          new_cols += col + ","
    
      return new_cols[0: -1]
    
    def Meta_Layer(pth):
      global theDictStr, state
      state = 1
      indnt = '    '
      theDictStr += indnt + '"Meta_Layer" : {\n'
      getkeyvalues(0, indnt, pth)
      theDictStr += indnt + '},\n'
    
    def Layer_Column_Description(spc, pth):
      global theDictStr, state
      state = 2
      indnt = '    ' + spc
      theDictStr += indnt + '"Layer_Column_Description" : {\n'
      getkeyvalues(0, indnt, pth)
      theDictStr += indnt + '},\n'
    
    def Meta_LinkTable(pth):
      global theDictStr, state
      state = 3
      indnt = '    '
      theDictStr += indnt + '"Meta_LinkTable" : {\n'
      getkeyvalues(1, indnt, pth)
      theDictStr += indnt + '},\n'
    
    def LinkTable_Column_Description(spc, pth):
      global theDictStr, state
      state = 4
      indnt = '      ' + spc
      theDictStr += indnt + '"LinkTable_Column_Description" : {\n'
      ret = getkeyvalues(0, indnt, pth)
      theDictStr += indnt + '},\n'
      return ret
    
    def Theme_Layer_Mapping(pth):
      global theDictStr, state
      state = 5
      indnt = '    '
      theDictStr += indnt + '"Theme_Layer_Mapping" : {\n'
      getkeyvalues(0, indnt, pth)
      theDictStr += indnt + '},\n'
    
    def Global_Resource_Mapping(pth):
      global theDictStr, state
      state = 6
      indnt = '    '
      theDictStr += indnt + '"Global_Resource_Mapping" : {\n'
      #getkeyvalues(1, indnt, pth)
      getDirectMappingDict(indnt)
      theDictStr += indnt + '},\n'
    
    def Record_Types(pth):
      global theDictStr, state
      state = 7
      indnt = '    '
      theDictStr += indnt + '"Record_Types" : {\n'
      #getkeyvalues(1, indnt, pth)
      getDirectMappingDict(indnt)
      theDictStr += indnt + '},\n'
    
    def getDirectMappingDict(indnt):
      global theDictStr, fl_lines, fl_len, cursr, err, columnnamekeys
    
      i = 0
      indnt = indnt + '  '
      spc = '  '
    
      DirectMappingDict = {}
    
      #theDictStr += indnt + '"' + str(i) + '" : {\n'
      while cursr < fl_len:
        line = fl_lines[cursr].strip()
    
        if line == '#':
          #theDictStr += indnt + '},\n'
          appendDirectMappingDicttoglobaldict(indnt, i, DirectMappingDict)
          i += 1
          #theDictStr += indnt + '"' + str(i) + '" : {\n'
          cursr += 1
          DirectMappingDict = {}
          continue
        elif line == '':
          cursr += 1
          continue
        elif line.startswith('*'):
          cursr -= 1
          appendDirectMappingDicttoglobaldict(indnt, i, DirectMappingDict)
          return '*'
        else:
          key, sep, val = line.partition(':')
          key = key.strip().replace("\t", " ")
          if key.endswith("tablename"):
            val = val.replace(".dbf", "").replace(".txt", "")
          if key.endswith("column") or key.endswith("columns"):
            val = val.lower().replace("'", "''")
          DirectMappingDict[key] = val.strip()
          cursr += 1
    
      appendDirectMappingDicttoglobaldict(indnt, i, DirectMappingDict)
    
    def appendDirectMappingDicttoglobaldict(indnt, i, DirectMappingDict):
      global theDictStr, g_layer_tablename, g_link_tablenames
      if(DirectMappingDict["table_type"].lower() == "layer"):
        DirectMappingDict['tablename'] = g_layer_tablename
      elif(DirectMappingDict["table_type"].lower() == "link"):
        DirectMappingDict['tablename'] = g_link_tablenames[DirectMappingDict['tablename']]
    
      theDictStr += indnt + '"' + str(i) + '" : \n'
      theDictStr += indnt + str(DirectMappingDict) + '\n'
      theDictStr += indnt + ',\n'
    
    def getblock(line, pth):
      global err
      if err == 1:
        return 
      {
        'Meta_Layer': Meta_Layer,
        #'Layer_Column_Description': Layer_Column_Description,
        'Meta_LinkTable': Meta_LinkTable,
        #'LinkTable_Column_Description': LinkTable_Column_Description,
        'Theme_Layer_Mapping': Theme_Layer_Mapping,
        'Global_Resource_Mapping': Global_Resource_Mapping,
        'Record_Types': Record_Types
      }[line](pth)
    
    def getlayermetadata(filename, pth):
      global theDictStr, cursr, fl_len, fl_lines, err
      theDictStr = ""
      cursr = 0
      err = 0
      f = open(filename)
      try:
        fl_lines = f.readlines()
        fl_len = len(fl_lines)
        theDictStr += "  {\n"
        while cursr < fl_len:
          line = fl_lines[cursr].strip()
          if line.startswith('*'):
            cursr += 1
            getblock(line.replace('*', ''), pth)
          cursr += 1
        theDictStr += "  },\n"
        try:
          eval(theDictStr)
        except:
          print "\n\n%s\n\n" % theDictStr
          print "@REM #Error in metadata: " + filename
          theDictStr = ""
      finally:
        f.close()
    
    def getlayerinfo(pth):
      global theDictStr, err
    
      theFinalDictStr = '[\n'
      for root, dirs, files in os.walk(pth):
       for dirname in dirs:
         if dirname == "final":
            pth1 = os.path.join(root, dirname)
            #dbffl = ""
            shpfl = ""
            if os.name == 'nt':
              pth1_sp = pth1.split("\\")
            else:
              pth1_sp = pth1.split("/")
            
            lyr_name = str(pth1_sp[len(pth1_sp)-2])
            if( not (os.path.exists(pth1+"/metadata.txt") or os.path.exists(pth1+"/meta.txt"))):
              global DBNAME, DBUSER
              os.chdir(pth1)
              os.system("python "+cwdir+"/genMetaData.py "+lyr_name+" "+DBNAME+" "+DBUSER+" metadata.txt")
              os.chdir(cwdir)   
    
            fls = os.listdir(pth1)
            for flname in fls:
              #if(flname.endswith(".dbf")):
              #  dbffl = os.path.join(pth1, flname)
              #el
            
              if(flname.endswith(".shp")):
                shpfl = os.path.join(pth1, flname)
              elif(flname.endswith("metadata.txt") or flname.endswith("meta.txt") ):
                metadatafl = os.path.join(pth1, flname)
                getlayermetadata(metadatafl, pth1)  
    
            if err == 1:
              continue
            spc = "    "
            str1 = spc + "'shp_filename': " + '"' + shpfl.replace("\\", "\\\\") + '",' + "\n"
            #str1 += spc + "'dbf_filename': " + '"' + dbffl.replace("\\", "\\\\") + '",' + "\n"
            #str1 += spc + "'layer_fields': " + '%s, \n' % get_fields(dbffl)
            indx = theDictStr.rfind("  },\n")
            theDictStr = theDictStr[0:indx] + str1 + theDictStr[indx:]
            theFinalDictStr += theDictStr
            theDictStr = ''
            cursr = 0
    
      theFinalDictStr += ']\n'
      return theFinalDictStr
    
    def addcolcomments(tablename, theDict):
      sql = ''
      for col in theDict:
        try:
          sql += """COMMENT ON COLUMN "%s"."%s" IS '%s';\n""" % (tablename, col.lower(), theDict[col].replace("'", "''"))
        except:
          print "@REM # -- "+col
          print "@REM # -- "+str(theDict[col])
      return sql
    
    def ThemeLayerMappingSql(themename):
      return '''INSERT INTO "Theme_Layer_Mapping" ("theme_id", "layer_id", "created_by", "created_date", "modified_by", "modified_date", "status") VALUES ((SELECT theme_id  FROM "Theme" WHERE theme_name = '%s'), (SELECT currval('"Meta_Layer_layer_id_seq"')), 1, now(), 1, now(), 1);\n''' % (themename)
    
    def parse_Meta_Layer(layer_tablename, layer_type, Meta_Layer):
      Meta_Layer_sql = '''INSERT INTO "Meta_Layer" ('''
      vals = ""
      layer_colcomments = """
    COMMENT ON COLUMN "%s".__mlocate__id IS 'ID';
    COMMENT ON COLUMN "%s".__mlocate__status IS 'Status';
    COMMENT ON COLUMN "%s".__mlocate__layer_id IS 'Layer ID';
    COMMENT ON COLUMN "%s".__mlocate__nid IS 'NID';
    COMMENT ON COLUMN "%s".__mlocate__created_by IS 'Created By';
    COMMENT ON COLUMN "%s".__mlocate__created_date IS 'Created Date';
    COMMENT ON COLUMN "%s".__mlocate__modified_by IS 'Modified By';
    COMMENT ON COLUMN "%s".__mlocate__modified_date IS 'Modified Date';
    COMMENT ON COLUMN "%s".__mlocate__validated_by IS 'Validated By';
    COMMENT ON COLUMN "%s".__mlocate__validated_date IS 'Validated Date';
    """
      layer_colcomments = layer_colcomments.replace("%s", layer_tablename)
      for x in Meta_Layer:
        #print x, layer[x]
        if(x != "shp_filename" and x != "theme_id" and x != "related_layers" and x != "page_info"):
          #if(x == "license"):
          #  #Meta_Layer_sql += '"licensing",'
          #  #vals += "'" + Meta_Layer[x].replace("'", "''") + "',"
          #  Meta_Layer_sql += '"' + x + '",'
          #  if(Meta_Layer[x] == ""):
          #    vals += "'(by)',"
          #  else:
          #    vals += "'" + Meta_Layer[x] + "',"
          #el
          if(x == "Layer_Column_Description"):
            layer_colcomments += addcolcomments(layer_tablename, Meta_Layer[x])
          elif(x == "layer_type"):
            pass
          elif(x == "min_scale"):
            Meta_Layer_sql += '"' + x + '",'
            if(Meta_Layer[x] == ""):
              vals += "'5',"
            else:
              vals += "'" + Meta_Layer[x] + "',"
          elif(x == "max_scale"):
            if(Meta_Layer[x] == ""):
              pass
            else:
              Meta_Layer_sql += '"' + x + '",'
              vals += "'" + Meta_Layer[x].replace("'", "''") + "',"
          else:
            Meta_Layer_sql += '"' + x + '",'
            vals += "'" + Meta_Layer[x].replace("'", "''") + "',"
    
      Meta_Layer_sql += '"layer_type",'
      vals += "'%s'," % layer_type
    
      Meta_Layer_sql = Meta_Layer_sql[0:len(Meta_Layer_sql)-1]
      vals = vals[0:len(vals)-1]
    
      Meta_Layer_sql = "%s) values (%s);" % (Meta_Layer_sql, vals)
    
      return Meta_Layer_sql, layer_colcomments
    
    def parse_Meta_LinkTable(Meta_LinkTable):
      global err
      Meta_LinkTable_sql = ''
      for i in Meta_LinkTable:
        link_tablename = ""
        linkTable_filename = ""
        linkTable = Meta_LinkTable[i]
        metalink_sql = '''INSERT INTO "Meta_LinkTable" ('''
        vals = ""
        LinkTable_Column_Description = {}
        for item in linkTable.items():
          if item[0] == 'LinkTable_Column_Description':
            LinkTable_Column_Description = item[1]
            #layer_colcomments = addcolcomments(layer_tablename, Meta_Layer[x])
          elif item[0] == 'linkTable_filename':
            linkTable_filename = item[1]
          elif item[0] == 'link_tablename':
            link_tablename = item[1]
            metalink_sql += '"' + item[0] + '",'
            vals += "'" + item[1].replace("'", "''") + "',"
          else:
            metalink_sql += '"' + item[0] + '",'
            vals += "'" + item[1].replace("'", "''") + "',"
    
        metalink_sql += '"layer_id",'
        vals += """(SELECT currval('"Meta_Layer_layer_id_seq"')),"""
    
        metalink_sql = metalink_sql[0:len(metalink_sql)-1]
        vals = vals[0:len(vals)-1]
    
        metalink_sql = "%s) values (%s);\n" % (metalink_sql, vals)
        createtable_sql, insert_sql = gen_LinkTable_Sql(link_tablename, linkTable_filename)
    
        colcomments_sql = """
    COMMENT ON COLUMN "%s".__mlocate__id IS 'ID';
    COMMENT ON COLUMN "%s".__mlocate__status IS 'Status';
    COMMENT ON COLUMN "%s".__mlocate__layerdata_id IS 'Layer row ID';
    COMMENT ON COLUMN "%s".__mlocate__metalink_id IS 'Link table ID';
    COMMENT ON COLUMN "%s".__mlocate__created_by IS 'Created By';
    COMMENT ON COLUMN "%s".__mlocate__created_date IS 'Created Date';
    COMMENT ON COLUMN "%s".__mlocate__modified_by IS 'Modified By';
    COMMENT ON COLUMN "%s".__mlocate__modified_date IS 'Modified Date';
    """
        colcomments_sql = colcomments_sql .replace("%s", link_tablename)
        colcomments_sql += addcolcomments(link_tablename, LinkTable_Column_Description)
    
        Meta_LinkTable_sql += "%s\n%s\n%s\n%s" % (createtable_sql, colcomments_sql, insert_sql, metalink_sql)
      if err:
        return ""
      else:
        return Meta_LinkTable_sql
    
    def parse_Global_Resource_Mapping(Global_Resource_Mapping):
      Global_Resource_Mapping_sql = ""
      for i in Global_Resource_Mapping:
        mapping = Global_Resource_Mapping[i]
        mapping["table_type"] = mapping["table_type"].upper()
        cols = map(lambda x: '"' + x + '"', mapping.keys())
        vals = map(lambda x: "'" + x + "'", mapping.values())
        sql = '''INSERT INTO "Global_Resource_Mapping" (%s) VALUES(%s);
        ''' % (','.join(cols), ','.join(vals))
        Global_Resource_Mapping_sql += sql
      return Global_Resource_Mapping_sql
    
    def parse_Record_Types(Record_Types):
      Record_Types_sql = ""
      for i in Record_Types:
        mapping = Record_Types[i]
        mapping["table_type"] = mapping["table_type"].upper()
        cols = map(lambda x: '"' + x + '"', mapping.keys())
        vals = map(lambda x: "'" + x + "'", mapping.values())
        sql = '''INSERT INTO "mlocate_data_record_types" (%s) VALUES(%s);
        ''' % (','.join(cols), ','.join(vals))
        Record_Types_sql += sql
      return Record_Types_sql
    
    def modifyCreateLayer(b):
      c = []
      insrtd_inherits = 0
      for i in range(1, len(b)):
        d = b[i].replace("\n", "")
        if(i == 2):
          d = d.replace("gid serial PRIMARY KEY,", "PRIMARY KEY (__mlocate__id),")
        if(d.endswith(");") and insrtd_inherits == 0):
          d = d[0:len(d)-1]
          c.append("  " + d)
          c.append('  INHERITS ("Layer_template");')
          insrtd_inherits = 1
        elif(d.endswith("END;")):
          pass
        elif(d.startswith("CREATE INDEX")):
          pass
        else:
          c.append("  " + d)
      return c
    
    def dbimport(theDict, sql_file):
      print "inside dbimport"
      global err
      for layer in theDict:
        print "layer: ", layer
        err = 0
        Meta_Layer = layer["Meta_Layer"]
        if not "layer_tablename" in Meta_Layer:
          print layer["shp_filename"]
        layer_tablename = Meta_Layer["layer_tablename"].replace(".dbf", "")
    
        cmmd = '%s -s -1 -W "latin1" -I -%s -g %s "%s" "%s" %s' % (shp2pgsql, "%s", GEOMCOL, layer["shp_filename"], layer_tablename, DBNAME)
        print "CMD: ", cmmd
        a,b = runCmd(cmmd % "p")
        print "a:", a
        print "b:", b
    
        layer_type = a[1].replace("Postgis type: ", "").replace("\n", "")
        indx = layer_type.find("[")
        if indx != -1:
          layer_type = layer_type[0:indx]
    
        c = modifyCreateLayer(b)
    
        Meta_Layer_sql, layer_colcomments = parse_Meta_Layer(layer_tablename, layer_type, Meta_Layer)
    
        print "cwdir: ", cwdir
        sqlfl = os.path.join(os.path.join(cwdir, "layersqls"), "%s.sql" % layer_tablename)
        print "sqlfl: ", sqlfl
        a,b = runCmd("%s > %s" %((cmmd % "a"), sqlfl))
    
        sql1 = "%s\n\n%s\n" % ('\n'.join(c), layer_colcomments)
        sql2 = '''\nUPDATE "%s" SET __mlocate__layer_id = (SELECT currval('"Meta_Layer_layer_id_seq"')), __mlocate__status = 1, __mlocate__created_by = 1, __mlocate__created_date = now(), __mlocate__modified_by = 1, __mlocate__modified_date = now(), __mlocate__validated_by = 1, __mlocate__validated_date = now();\n\n''' % layer_tablename
        sql2 = "\n%s\n%s\n" % (Meta_Layer_sql, sql2)
        #Theme_Layer_Mapping = layer["Theme_Layer_Mapping"]
        #sql2 += ThemeLayerMappingSql(Theme_Layer_Mapping["theme_id"])
        #sql2 += ThemeLayerMappingSql(Theme_Layer_Mapping["geo_id"])
    
        if "Meta_LinkTable" in layer:
          sql2 +=  "\n" + parse_Meta_LinkTable(layer["Meta_LinkTable"])
    
        global_resource_mapping_sql = ""
        if "Global_Resource_Mapping" in layer:
          global_resource_mapping_sql = parse_Global_Resource_Mapping(layer["Global_Resource_Mapping"])
        sql2 += global_resource_mapping_sql
    
        record_types_sql = ""
        if "Record_Types" in layer:
          record_types_sql = parse_Record_Types(layer["Record_Types"])
        sql2 += record_types_sql
    
        sql2 += createRoles(layer_tablename)
        
        if(err):
          print "@REM # Error in data\n"
          os.remove(sqlfl)
        else:
          insertSql(sqlfl, sql1, sql2)
          #convert_to_utf8(sqlfl)
          print "@REM # -- Related shape file: %s" % layer["shp_filename"]
          sql_file.write('''psql -d %s -U %s -f "%s" > logs/%s.log 2>&1\n''' % (DBNAME, DBUSER, sqlfl, layer_tablename))
    
    def createRoles(layer_tablename):
      global ROLE_TYPES
      sql = "\n"
      for role in ROLE_TYPES:
        sql += "insert into role(name) values ('%s %s');\n" % (layer_tablename, role)
        sql += "insert into permission (rid, perm) values ((select rid from role where name = '%s %s'), '%s');\n" % (layer_tablename, role, ROLE_PERSMISSIONS[role])
      sql += "\n"
      return sql
    
    def main(pth, sql_file_name):
        if(os.path.exists('layersqls')):
          shutil.rmtree('layersqls')
        os.mkdir('layersqls')
        if(os.path.exists('logs')):
          shutil.rmtree('logs')
        os.mkdir('logs')
        if(os.path.exists('layers.list')):
          os.remove('layers.list')
        y = getlayerinfo(pth)
        f1 = open("layers.list", "w")
        f1.write(str(y))
        f1.close()
        z = eval(y)
        sql_file = open(sql_file_name, "w")
        print "z: ", z
        print "sql_file: ", sql_file
        dbimport(z, sql_file)
        sql_file.close()
    
    # get the next auto increment value from sequence #
    def getnextseqfromdb(seqname):
      global DBNAME, DBUSER
      if os.name == 'nt':
        cmmd = '''psql -d %s -U %s -t -c "select last_value+increment_by from ""%s"" " ''' % (DBNAME, DBUSER, seqname) # Windows
      else:
        cmmd = '''psql -d %s -U %s -t -c 'select last_value+increment_by from "%s"' ''' % (DBNAME, DBUSER, seqname) # Linux
      a,b = runCmd(cmmd)
      return int(b[0].strip().replace("\n", ""))
    
    print "pth value: ", pth
    layer_seq = getnextseqfromdb("Meta_Layer_layer_id_seq")
    link_seq = getnextseqfromdb("Meta_LinkTable_id_seq")
    main(pth, sql_file_name)

def generate_geoserver_files(layers):
    from generate_geoserver_styles import generate_style
    from generate_geoserver_cache_config import generate_cache_config
    from generate_geoserver_layers import generate_layer_xml
    generate_style(layers, geoserver_data_dir_path);
    generate_cache_config(layers, geoserver_data_dir_path);
    generate_layer_xml(layers, geoserver_data_dir_path);

sql_file_name = "sql_cmds"
import_layers(dbname, dbuser, datapath, sql_file_name)

'''
status = os.system("python import_layers.py "+dbname+" "+dbuser+" "+datapath+" 1> std_op 2> err_op")
if status != 0:
	print "Error executing import_layers.py"
	sys.exit(1)

if os.name == 'nt':
	os.system("findstr \"psql\" std_op > sql_cmds.bat")
else:
	os.system("grep '^psql' std_op > sql_cmds;chmod +x sql_cmds")
'''
if os.name != 'nt':
    os.system("chmod +x " + sql_file_name)

# Add ROLLBACK to the sql scripts to verify if there are no errors.
'''
os.system("python test.py END ROLLBACK")
if os.name == 'nt':
	os.system("sql_cmds.bat > tmp")
	status = os.system("findstr /I /S \"error\" logs/*")
else:
	os.system("./sql_cmds")
	status = os.system("grep -irn 'error' logs/")
if status == 0:
	print "Error in the log files, go through logs/ dir to find out errors"
	sys.exit(1)
'''
# Since no errors revert ROLLBACK to END to commit data.
'''
os.system("python test.py ROLLBACK END")
if os.name == 'nt':
	os.system("sql_cmds.bat > tmp")
	status = os.system("findstr /I /S \"error\" logs/*")
else:
	os.system("./sql_cmds")
	status = os.system("grep -irn 'error' logs/")
if status == 0:
	print "Database updated, Error in the log files, go through logs/ dir to find out errors"
	sys.exit(1)
'''

os.system("./sql_cmds")
status = os.system("grep -irn 'error' logs/")
if status == 0:
	print "Database updated, Error in the log files, go through logs/ dir to find out errors"
	sys.exit(1)

layers = []
for i in os.listdir("layersqls"):
	layers.append(i.replace(".sql",""))

generate_geoserver_files(layers);
		
print "Data uploaded successfully!"
print "Layers added:"

#clean up
#shutil.rmtree("layersqls")
#shutil.rmtree("logs")
#os.remove("layers.list")
#os.remove("std_op")
#os.remove("err_op")

if os.name == 'nt':
	os.remove("sql_cmds.bat")
	os.remove("tmp")
else:
	os.remove("sql_cmds")

sys.exit(0)


