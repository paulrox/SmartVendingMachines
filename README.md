# SmartVendingMachines
IoT infrastructure for the management of a set of distributed vending machines

# Run the Application

The project consists in several different applications that need to be executed in a precise order.

## Start Simulation

First, it must be executed Cooja and opened one of the two simulations provided in the folder "Simulations". It is warmly recommended to use the "final_simul_3_mote" simulation instead the other one because it is configured with a minor number of motes (three motes + one BR). If the user has not a sufficient hardware capability, it could compromise the behavior of the application.

```Shell
sh runCooja &
/∗ Open the simulation and start it.∗/
cd Contiki/rpl−border−router−cooja
make connect−router−cooja
```

## Start CSE Application
CSE of the MN and of the IN must be start first.

###IN-CSE
```Shell
cd Infrastructure/OM2M/IN_CSE
sh start.sh
```

###MN-CSE
```Shell
cd Infrastructure/OM2M/MN_CSE
sh start.sh
```
## Start ADN Applications
After the CSE applications, the ADN must be executed. The IN ADN must be executed as last and only after the MN has finished to discover the VMs, the ADN IN could be executed. After that, it could take a while for a setup. The ADN IN waits that the ADN MN has retrieved all the resource values and created all the content instances and this process is very slow with a large number of motes.

### Build the ADNs
```Shell
cd Infrastructure/SVM/mvn clean install
```
### MN-ADN
```Shellcd Infrastructure/SVM/SVM_MNjava −jar target/SVM_MN−0.0.1−SNAPSHOT−jar−with−dependencies.jar
```
### IN-ADN
```Shellcd Infrastructure/SVM/SVM_INjava −jar target/SVM_IN−0.0.1−SNAPSHOT−jar−with−dependencies.jar
```## Start Web Application
Open "index.html" in the web browser, located in the ```WebApp``` folder.
## Authors

* **Paolo Sassi** - [paulrox](https://github.com/paulrox)
* **Matteo Rotundo** - [Gyro91](https://github.com/Gyro91)
