zklogtool
=========

Zklogtool is a ZooKeeper transactions and data tree monitoring tool. It works by analyzing transaction logs in real time. It can reconstruct ZooKeepers data tree at any particular point in time. That works by loading data tree from snapshot file and applying transactions to it.

Zklogtool project consists of zklogtool library, console application and web application. 

Possible use cases are:
- Learning ZooKeeper
- Developer tool
- Troubleshooting
- Integration in test environment
- Integration in system


##Build and test

Zklogtool is multi-module maven project. To build and run tests navigate to projects root folder and execute:

```
mvn verify
```

Build takes several minutes to complete.


##How to use

There are bash and windows batch scripts for running both console application and web application.  Web application can be deployed to any servlet container. Build process downloads jetty and jetty-runner so you can try it out without any setup. This examples will show you how to monitor transactions in real time and how to reconstruct  data tree after particular transaction. 

###Console application

Go to zklogtool/target directory and execute:

```
./zklogtool.sh log -data-log-dir [path to your logs]
```
![alt text](http://i.imgur.com/FYYugtJ.png "Transaction log example")

To reconstruct data tree after particular transaction execute:

```
./zklogtool.sh snapshot -data-dir [path to your data directory] -zxid [zxid]
```
![alt text](http://i.imgur.com/cFuyQAe.png "Snapshot example")

These are all options:

```
Usage: zklogtool [options] [command] [command options]
  Options:
        --help
       Print help message
       Default: false
        --version
       Print zklogtool version
       Default: false
  Commands:
    log      Display transaction log entries
      Usage: log [options]
        Options:
              --follow
             Output appended data as the transactions are written to logs
             Default: false
              --start-with-last-transaction
             Start printout from last written transaction. Can only be used with
             follow option
             Default: false
          -data-decoder
             Decoder used to display znode's data byte array
             Default: UnicodeDecoder
          -data-log-dir
             Zookeeper log direcory path
          -log-file
             Zookeeper transaction log file path
          -properties-file
             Zookeeper configuration file path

    snapshot      Display zookeeper data tree at certain time
      Usage: snapshot [options]
        Options:
          -data-decoder
             Decoder used to display znode's data byte array
             Default: UnicodeDecoder
          -data-dir
             Zookeeper data direcory path
          -data-log-dir
             Zookeeper log direcory path. If not provided dataDir is used
          -properties-file
             Zookeeper configuration file path
          -snap-file
             Zookeeper snapshot file. Data is fuzzy.
          -zxid
             Hex value of last commited zxid. Strings first and last can also be
             used
             Default: last

```

###Web application

To start web app with jetty server go to zklogtoolwebapp/target and execute:

```
./zklogtoolwebapp.sh
```

Now navigate to localhost:8080 in your browser:

To display transaction log go to File -> Open transaction log and fill the form and click Open.

![alt text](http://i.imgur.com/Jk134KU.png "Transaction log example")

To reconstruct data tree after particular transaction click on transaction and than click on Reconstruct Data Tree button. 

![alt text](http://i.imgur.com/tWJlv7f.png "Snapshot example")

For real time monitoring use `--follow` flag in console application or check Follow check-box in web application.


##API

API is simple and well documented. Please refer to javadoc for details, this is just a quick overview.  To generate javadoc run following command in projects root folder:
```
mvn javadoc:javadoc
```

This example shows how to analyze transaction log:
```java
import com.zklogtool.data.*;
import com.zklogtool.monitor.*;
import com.zklogtool.reader.*;
import java.io.File;


public class transactionlogexample {

    public static void main(String[] args) {
        
        TransactionLogReaderFactory factory = new TransactionLogReaderFactory();
        TransactionLogFileList logs = new DataDirTransactionLogFileList(new File("/tmp/zookeeper/version-2/"));
        
        TransactionLog transactionLog = new TransactionLog(logs,factory);
        
        //this is simple way
        for (Transaction t:transactionLog){
        
            System.out.println("Zxid: "+t.getTxnHeader().getZxid());
        
        }
        
        //this is more powerfull way
        TransactionIterator i = transactionLog.iterator();        
        boolean exitLoop = false;
        
        do{
        
            switch(i.nextTransactionState()){
                
                case OK:
                    System.out.println("Transaction is entirely written, zxid: "+i.next().getTxnHeader().getZxid());
                    break;
                case INCOMPLETE:
                    System.out.println("Transaction is not entirely written");
                    exitLoop=true;
                    break;
                case CORRUPTION:
                    System.out.println("Transaction log is corrupted");
                    exitLoop=true;
                    break;
                case EMPTY:
                    System.out.println("Transaction not written");
                    exitLoop=true;
                    break;
            
            }
        
        } while(!exitLoop);
        
        //monitoring transaction log is simple
        TransactionMonitor monitor = new TransactionMonitor(transactionLog);
        
        monitor.addListener(new TransactionListener(){

            @Override
            public void onTransaction(Transaction t) {
                System.out.println("Zxid: "+t.getTxnHeader().getZxid());
            }

            @Override
            public void onPartialTransaction() {
                System.out.println("Transaction is not entirely written");
            }

            @Override
            public void onCorruption() {
                System.out.println("Transaction log is corrupted");
            }
        
        });
        
        //starts monitoring thread
        monitor.startAtFirstTransaction();
        
    }
    
}
```

This example shows how to load snapshot file:

```java
import com.zklogtool.data.*;
import com.zklogtool.reader.*;
import java.io.File;
import java.io.IOException;
import java.util.Date;


public class datatreeexample {
    

    public static void main(String[] args) throws IOException, Exception {
        
        File snapFile = new File("/tmp/zookeeper/version-2/snapshot.3");
        SnapshotFileReader snapReader = new SnapshotFileReader(snapFile,3);
        
        //DataState holds entire ZooKeeper data tree and sessions
        DataState dataState;
        
        //read fuzzy snapshot
        dataState = snapReader.readFuzzySnapshot();
        
        //reconstruct data tree to ensure it is not fuzzy
        //refer to documentation for more details
        
        TransactionLogReaderFactory factory = new TransactionLogReaderFactory();
        TransactionLogFileList logs = new DataDirTransactionLogFileList(new File("/tmp/zookeeper/version-2/"));   
        TransactionLog transactionLog = new TransactionLog(logs,factory);
        dataState = snapReader.restoreDataState(transactionLog.iterator());
        
        //now you can iterate data tree
        for (String s : dataState.getChildren("/")){
            
            System.out.println("Node "+s+" created at "+ new Date(dataState.getNode("/"+s).stat.getCtime()));
            
        }
        
        //applying transactions is easy
        //dataState.processTransaction(Tranasction t);
        
    }

}
```


##Licence
Zklogtool is licenced under Apache Software Licence 2.0.
 



