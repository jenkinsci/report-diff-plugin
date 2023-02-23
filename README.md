# jenkins-report-diff
Jenkins plugin to monittor comaprable content on system

* [Job run details](#job)
* [Project state graph](#project)
    * [tooltip](#tooltip)
* [Settings](#settings)
* [Limitations](#limitations)
    
This plugin allows you to monitor any difference in output of file or of rnu of any command, where unit is an line.
Exemplar usage is `rpm -qa` - then during every run, all packages on system are stored, and during next run they are compared.

## Job
Most detaield view cnabe shown from build screen
![koji-jtreg-rpms](https://user-images.githubusercontent.com/2904395/43510354-a8f3e86c-9575-11e8-9318-c3516d65a876.png)
In job build screen, simple, but a lot of saying  ```RPM Changes: X RPMs installed, Y RPMs removed``` is provided, where **RPM Changes** is link to following screen.
Note, that Rpm Changes, Rpms installed and RPMs removed are **custom** strings which describe the changes you are monitoring.  See *advanced* section in job config of plugin. 
 
![run](https://user-images.githubusercontent.com/2904395/43015810-eb0c74fe-8c50-11e8-8420-ec3fb8df6037.png)
You can nicely see what lines (packages in our case)  had changed.

## Project
For quick overview, you have in-project graph:
![project](https://user-images.githubusercontent.com/2904395/43015811-eb2dc942-8c50-11e8-9bd7-56e71254c7f0.png)
this graph shows  how many lines was in stream, how many lines were removed compared to previous,  how many lines were added.

### Tooltip
The tooltip says it in manner of original purpose - difference in lines (installed packages in our example:
![tooltip](https://user-images.githubusercontent.com/2904395/43015812-eb4dbe50-8c50-11e8-81fa-b22cc9d0458c.png)
Unluckily the total - blackline, added - green line and removed as red line in one graph was not happy choice. Usually minor adds/removes are not visible. Wehn we do an system update, graph loks like:
![update](https://user-images.githubusercontent.com/2904395/43122742-61016178-8f22-11e8-8817-28d6da1dc57e.png)
So although you see major changes, the red and green lines get mixed.

## Settings
When you look to the sedttings:
![settings](https://user-images.githubusercontent.com/2904395/43122741-60e40448-8f22-11e8-84c2-de47d9c8e4be.png)
You can see it is comand or file what you put here. It operates on workspace, so the file do not need to be archived (as is celar from nature of command xor file)

## Limitations
This plugin depends on https://github.com/judovana/jenkins-chartjs-plugin
