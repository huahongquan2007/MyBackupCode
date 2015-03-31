/**********************************************************************************************
This code is part of the code supplied with the OpenCV Blueprints book.
It was written by Steven Puttemans, who can be contacted via steven.puttemans[at]kuleuven.be
***********************************************************************************************
This is software for making a summation file of folder contents.

USAGE:
./folder_listing -folder <folder> -output <list.txt>
***********************************************************************************************/

// Includes for standard output functionality
#include <stdlib.h>
#include <sstream>
#include <iostream>
#include <cstring>

using namespace std;

int main( int argc, const char** argv )
{
	// Retrieve the input parameters supplied as arguments to the function
    string folder;
    string output;
    for(int i = 1; i < argc; ++i )
    {
        if( !strcmp( argv[i], "-folder" ) )
        {
            folder = argv[++i];
        }else if( !strcmp( argv[i], "-output" ) )
        {
            output = argv[++i];
        }
    }

	// Create the batch command
	stringstream buffer;
	buffer << "find " << folder << " -type f > " << output;

    // Execute the batch command
    system( buffer.str().c_str() );

    return 0;
}
