'''
Created on Dec 1, 2018

@author: anatole

'''

# import simplejson, urllib.request
import googlemaps 

def getDistanceBoulot(orig_lat, orig_lng):
    orig_coord = orig_lat, orig_lng
    dest_coord = 43.563459, 1.496918
#     url = "https://maps.googleapis.com/maps/api/distancematrix/json?origins={0}&destinations={1}&mode=bicycling&language=en-EN&sensor=false&key=AIzaSyDUoQ_XaZBeK9-kHSUnm1QZ_G_BoXE_jlw".format(str(orig_coord),str(dest_coord))
#     url = "http://avi.im/stuff/js-or-no-js.html"
  
    # Requires API key 
    gmaps = googlemaps.Client(key='AIzaSyDUoQ_XaZBeK9-kHSUnm1QZ_G_BoXE_jlw') 
      
    # Requires cities name 
    my_dist = gmaps.distance_matrix(f'{orig_coord[0]}, {orig_coord[1]}',f'{dest_coord[0]}, {dest_coord[1]}',mode='bicycling')['rows'][0]['elements'][0] 
      
    # Cycling time in minutes
    cycling_time = my_dist['duration']['value']/60
    return cycling_time