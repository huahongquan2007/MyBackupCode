#ifndef _AIOS_CORE_VISION_UTILS_H_
#define _AIOS_CORE_VISION_UTILS_H_

// get current time in milliseconds
#include <time.h>
#include <cmath>
using namespace std;
// from android samples
/* return current time in milliseconds */
static double now_ms(void) {

    struct timespec res;
    clock_gettime(CLOCK_REALTIME, &res);
    return 1000.0 * res.tv_sec + (int) (res.tv_nsec / 1e6);
}

static double calculateDistance(double x1, double x2, double y1, double y2) {
	return sqrt(pow((x1 - x2), 2.0) + pow((y1 - y2), 2.0));
}
// end get time in milliseconds

#endif