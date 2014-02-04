#ifndef _STOPWATCH_H
#define _STOPWATCH_H

#include <ctime>

class StopWatch {
private:
     clock_t mStart;
     clock_t mEnd;
public:
     StopWatch() : mStart(0), mEnd(0) {}
     inline void start() { mStart = clock(); }
     inline void stop() { mEnd = clock(); }
     inline float secs() const { return static_cast<float>(mEnd - mStart) / 
               static_cast<float>(CLOCKS_PER_SEC); }
     inline float tic() const { return 1.0 / static_cast<float>(CLOCKS_PER_SEC); }
};

#endif   // _STOPWATCH_H
