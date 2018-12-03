'''
Created on Dec 3, 2018

@author: anatole

'''

def getKey():
    f=open("../../googlemapsapi/key", "r")
    key =f.read()
    return key
