import os,sys,shutil

status = os.system("python import_layers.py "+dbname+" "+dbuser+" "+datapath+" 1> std_op 2> err_op")
if status != 0:
	print "Error executing import_layers.py"
	sys.exit(1)

if os.name == 'nt':
	os.system("findstr \"psql\" std_op > sql_cmds.bat")
else:
	os.system("grep '^psql' std_op > sql_cmds;chmod +x sql_cmds")

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


print "Data uploaded successfully!"
print "Layers added:"

for i in os.listdir("layersqls"):
	print i.replace(".sql","")
		
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

