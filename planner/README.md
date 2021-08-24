//ca sa muti un fisier local pe cloud, scrii comanda dintr-un terminal normal
scp -i "./TeamRocket-ec2key.pem" planner-0.0.1-SNAPSHOT.jar ec2-user@ec2-18-184-175-183.eu-central-1.compute.amazonaws.com:/home/ec2-user

//ca sa te conectezi la cloud cu comanda asta
ssh -i "TeamRocket-ec2key.pem" ec2-user@ec2-18-184-175-183.eu-central-1.compute.amazonaws.com

//ca sa verifici daca ruleaza deja app-ul
top

//ca sa pornesti/inchizi app-ul
./server_start.sh
./server_stop.sh

//link-ul (momentan)
https://dualstack.teamrocket-coolloadbalanacer-1332110150.eu-central-1.elb.amazonaws.com/healthcheck