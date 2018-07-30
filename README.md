# jenkins-report-rpms
Jenkins plugin to create the list of RPMs installed in the system

* [Job run details](#job)
    * [Project state graph](#project)
* [tooltip](#tooltip)
* [Settings](#settings)
* [Limitations](#limitations)
    
This plugin allows you to monitor changes of packages on your system. It can actually observe difference between any stream-able source (like file) where unit is an line.

## Job
Most detailed is project's run view:
![run](https://user-images.githubusercontent.com/2904395/43015810-eb0c74fe-8c50-11e8-8420-ec3fb8df6037.png)
You can nicely see what lines (packages in our case)  had changed.

## Project
For quick overview, you have in-project graph:
![project](https://user-images.githubusercontent.com/2904395/43015811-eb2dc942-8c50-11e8-9bd7-56e71254c7f0.png)
this graph shows  how many lines was in stream, how many lines were removed compared to previous,  how many files were added.

### Tooltip
The tooltip says it in manner of original purpose - difference in installed packages software:
![tooltip](https://user-images.githubusercontent.com/2904395/43015812-eb4dbe50-8c50-11e8-81fa-b22cc9d0458c.png)
Unluckily the total - blackline, added - green line and removed as red line in one graph was not happy choice. Usually minor adds/removes are not visible. Wehn we do an system update, graph loks like:
![update](https://user-images.githubusercontent.com/2904395/43122742-61016178-8f22-11e8-8817-28d6da1dc57e.png)
So although you see major changes, the red and green lines get mixed.

## Settings
When you look to the sedttings:
![settings](https://user-images.githubusercontent.com/2904395/43122741-60e40448-8f22-11e8-84c2-de47d9c8e4be.png)
You can see it really is comand, not file what you put here. By cat you simply read archived file. We were experimenting with direct "rpm -qa"  or "ssh machine "cat or rpm query" so the file was not requested to be archived, nor the script should require list generation. However that resolved to be tricky - sometimes the provider was vm, sometimes HW host, sometimes it crashed and so. So the cat from archived list was sad winner.

## Limitations
Long story short, this plugin, however versatile have issues in quick readability. Still we find it useful and so may you

This plugin depends on https://github.com/judovana/jenkins-report-generic-chart-column
