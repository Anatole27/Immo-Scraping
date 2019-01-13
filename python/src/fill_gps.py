'''
Created on Dec 2, 2018

@author: anatole
'''

import csv 
import find_location as loc

with open('../res/leboncoin_2018_12_01.csv', encoding = "ISO-8859-1") as csv_file:
    csv_reader = csv.reader(csv_file, delimiter=',')
    line_count = 0
    for row in csv_reader:
        if line_count == 0:
            print(f'Column names are {", ".join(row)}')
            line_count += 1
        else:
            print(f'\t{row[3]}')
            line_count += 1
            
            # Find location
            finder = loc.LocFinder();
            coords = finder.findLocation(row[3])
            print(coords.lat)
            print(coords.lon)
            finder.closeBrowser()
        
            if line_count > 5:
                break
    print(f'Processed {line_count} lines.')