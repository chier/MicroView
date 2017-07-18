# coding=UTF-8
## 绘制图形

__author__ = 'zhangbo'
import psycopg2
import os
#import arcpy
import matplotlib.pyplot as plt
import numpy as np
import math
import sys
import json
#导入创建唯一编码模块
import uuid
#导入日志及pg模块  
import logging  
import logging.config

#日志配置文件名  
LOG_FILENAME = 'logging.conf'  
  
#日志语句提示信息  
LOG_CONTENT_NAME = 'pg_log'  
  
def log_init(log_config_filename, logname):  
    ''''' 
    Function:日志模块初始化函数 
    Input：log_config_filename:日志配置文件名 
           logname:每条日志前的提示语句 
    Output: logger 
    author: *** 
    date:2015-10-29
    '''
    path = sys.path[0]
    logging.config.fileConfig(path+'\\'+log_config_filename)  
    logger = logging.getLogger(logname)  
    return logger

def plotxy(sql,xF,yF,cType):
    '''''
    Function:操作pg数据库函数 
    Input：NONE 
    Output: NONE 
    author: socrates 
    date:2012-02-12 
    '''    
    pgdb_logger.debug("operate_postgre_tbl_product enter...")

    #连接数据库    
    try:  
        pgdb_conn = psycopg2.connect(database="Disaster_database", user="Allenaya", password="truth", host="192.168.1.110", port="5432")
    except Exception, e:  
        print e.args[0]  
        pgdb_logger.error("connect postgre database failed, ret = %s" % e.args[0])      
        return

    pgdb_logger.info("connect postgre database(Disaster_database) succ.")

    '''''
    #删除表  
    sql_desc = "DROP TABLE IF EXISTS tbl_product3;"  
    try:  
	pgdb_conn.query(sql_desc)  
    except Exception, e:  
	print 'drop table failed'  
	pgdb_logger.error("drop table failed, ret = %s" % e.args[0])  
	pgdb_conn.close()    
	return  
      
    pgdb_logger.info("drop table(tbl_product3) succ.")

    #创建表  
    sql_desc = 'CREATE TABLE tbl_product3( 
        i_index INTEGER, 
        sv_productname VARCHAR(32) 
        );'  
    try:      
        pgdb_conn.query(sql_desc)  
    except Exception, e:  
        print 'create table failed'  
        pgdb_logger.error("create table failed, ret = %s" % e.args[0])  
        pgdb_conn.close()    
        return          
     
    pgdb_logger.info("create table(tbl_product3) succ.")   
        
    #插入记录     
    sql_desc = "INSERT INTO tbl_product3(sv_productname) values('apple')"  
    try:  
        pgdb_conn.query(sql_desc)  
    except Exception, e:  
        print 'insert record into table failed'  
        pgdb_logger.error("insert record into table failed, ret = %s" % e.args[0])  
        pgdb_conn.close()    
        return      
       
    pgdb_logger.info("insert record into table(tbl_product3) succ.")       
       
    #查询表 1         
    sql_desc = "select * from tbl_product3"  
    for row in pgdb_conn.query(sql_desc).dictresult():  
        print row  
        pgdb_logger.info("%s", row)   
   
    #查询表2          
    sql_desc = "select * from tbl_test_port"  
    for row in pgdb_conn.query(sql_desc).dictresult():  
        print row   
        pgdb_logger.info("%s", row)
    '''

    xdata = [];
    ydata = [];
    cur = pgdb_conn.cursor()
    cur.execute(sql)
    #转换数据，字典格式
    data = [dict((cur.description[i][0], value) for i, value in enumerate(row)) for row in cur.fetchall()]
    for row in data:
        xdata.append(row[xF])
        ydata.append(row[yF])
    
    #关闭数据库连接
    cur.close()
    pgdb_conn.close()
    pgdb_logger.info("close postgre database(Disaster_database) succ.")
#    pgdb_logger.debug("draw_"+cType+"_chart leaving...")

    '''''
    conn.commit()
    cur.close()
    conn.close()
    '''
    
#x=json.loads(a)
#y=np.random.random((b,1))
    if cType == 'plot':
        plt.plot(xdata,ydata)
    elif cType == 'subplot':
        plt.subplot(1,1,1)
        plt.plot(xdata,ydata)
    else:
        plt.plot(xdata,ydata)

    pgdb_logger.info("draw_"+cType+"_chart succ.")
    imagename = str(uuid.uuid1())
    path = sys.path[0]
    plt.savefig(path[:path.rfind('\\')]+'\\tempimg\\'+imagename+'.png')
    pgdb_logger.info("save_"+imagename+" succ.")
    print imagename
#plt.show()

if __name__ == '__main__':   
      
    #初始化日志系统  
    pgdb_logger = log_init(LOG_FILENAME, LOG_CONTENT_NAME)     
      
    #执行操作（操作数据库）
    plotxy(sys.argv[1],sys.argv[2],sys.argv[3],sys.argv[4])

#print sys.argv[1]
