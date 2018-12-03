'''
Created on Dec 1, 2018

@author: anatole
'''
import unittest
import find_location as loc


class Test(unittest.TestCase):


    def testFindLocation(self):
        url = 'https://www.leboncoin.fr/ventes_immobilieres/1512512723.htm/';

        finder = loc.LocFinder();
        coords = finder.findLocation(url)
        print(coords.lat)
        print(coords.lon)
        finder.closeBrowser()

if __name__ == "__main__":
    #import sys;sys.argv = ['', 'Test.testFindLocation']
    unittest.main()