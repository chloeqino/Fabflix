from numpy import mean

file = open("log.txt","r")
count=0
ts_sum=0
tj_sum=0
while True:
  line = file.readline()
  line = line.strip()
  if not line:
    break
  count+=1

  ts,tj = line.split(",")
  ts_sum+=int(ts)
  tj_sum+=int(tj)

  
file.close()
print('ts average: ',ts_sum/count)
print('tj average: ',tj_sum/count)


