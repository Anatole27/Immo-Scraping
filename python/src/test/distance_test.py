'''
Created on Dec 1, 2018

@author: anatole
'''
import unittest
import distance


class Test(unittest.TestCase):


    def testDistance(self):
        orig = 43.563459, 1.476918

        res = distance.getDistanceBoulot(orig[0], orig[1])
        print(res)

if __name__ == "__main__":
    #import sys;sys.argv = ['', 'Test.testFindLocation']
    unittest.main()