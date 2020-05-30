#!/usr/bin/python
#
# percentage.py
# 2020/04/08
#
# Description: Returns the percentage os the two given values from nagios jmx
# Example of input: JMX OK AvailableSessions=8 JMX OK Size=10
# Example of output 0.8
#
# Silverio Santos
# NO RIGHTS RESERVED. Use as you want and enjoy! :P
#
import sys
import os
import argparse
import re
import math

parser = argparse.ArgumentParser(description='Merges multiple checks')
parser.add_argument('--output1', '-o1', required=True, help='first check output to parse')
parser.add_argument('--output2', '-o2', required=True, help='second check output to parse')


parser.set_defaults(debug=False)
args = parser.parse_args()


captured_1=args.output1
captured_2=args.output2
#print (captured_1)
valor1 = re.findall(r'\d', captured_1)
valor2 = re.findall(r'\d', captured_2)

#print(valor1)
v1=float(valor1[0])
v2=float(valor2[0])

perce=round(v1/v2,2)*100
#print v1
#print v2
print (perce)
