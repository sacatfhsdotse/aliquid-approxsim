// cribbed from https://code.google.com/p/segvcatch/

#include <signal.h>
#include <sys/syscall.h>
#include <unistd.h>
#include <stdexcept>
#include <execinfo.h>

namespace segvcatch
{
    typedef void (*handler)();
}

namespace
{

segvcatch::handler handler_segv = 0;

#if defined __GNUC__ && __linux

#define SIGNAL_HANDLER(_name)                                   \
static void _Jv_##_name (int, siginfo_t *,                      \
                         void *_p __attribute__ ((__unused__)))

extern "C"
{
  struct kernel_sigaction
  {
    void (*k_sa_sigaction)(int,siginfo_t *,void *);
    unsigned long k_sa_flags;
    void (*k_sa_restorer) (void);
    sigset_t k_sa_mask;
  };
}

#define RESTORE(name, syscall) RESTORE2 (name, syscall)

#ifdef __i386__
#define RESTORE2(name, syscall)                 \
asm                                             \
  (                                             \
   ".text\n"                                    \
   ".byte 0  # Yes, this really is necessary\n" \
   "    .align 16\n"                            \
   "__" #name ":\n"                             \
   "    movl $" #syscall ", %eax\n"             \
   "    int  $0x80"                             \
   );
#endif

#ifdef __x86_64__
#define RESTORE2(name, syscall)                 \
asm                                             \
  (                                             \
   ".text\n"                                    \
   ".byte 0  # Yes, this really is necessary\n" \
   ".align 16\n"                                \
   "__" #name ":\n"                             \
   "    movq $" #syscall ", %rax\n"             \
   "    syscall\n"                              \
   );
#endif

/* The return code for realtime-signals.  */
RESTORE (restore_rt, __NR_rt_sigreturn)
void restore_rt (void) asm ("__restore_rt")
  __attribute__ ((visibility ("hidden")));

#define INIT_SEGV                                               \
do                                                              \
  {                                                             \
    struct kernel_sigaction act;                                \
    act.k_sa_sigaction = _Jv_catch_segv;                        \
    sigemptyset (&act.k_sa_mask);                               \
    act.k_sa_flags = SA_SIGINFO|0x4000000;                      \
    act.k_sa_restorer = restore_rt;                             \
    syscall (SYS_rt_sigaction, SIGSEGV, &act, NULL, _NSIG / 8); \
  }                                                             \
while (0)

/* SEGV, ABRT, what's the difference. */
#define INIT_ABRT                                               \
do                                                              \
  {                                                             \
    struct kernel_sigaction act;                                \
    act.k_sa_sigaction = _Jv_catch_segv;                        \
    sigemptyset (&act.k_sa_mask);                               \
    act.k_sa_flags = SA_SIGINFO|0x4000000;                      \
    act.k_sa_restorer = restore_rt;                             \
    syscall (SYS_rt_sigaction, SIGABRT, &act, NULL, _NSIG / 8); \
  }                                                             \
while (0)

void default_segv()
{
    throw std::runtime_error("Segfault or something");
}

void handle_segv()
{
    if (handler_segv)
        handler_segv();
}

/* Unblock a signal.  Unless we do this, the signal may only be sent
   once.  */
static void unblock_signal(int signum __attribute__((__unused__)))
{
#ifdef _POSIX_VERSION
    sigset_t sigs;
    sigemptyset(&sigs);
    sigaddset(&sigs, signum);
    sigprocmask(SIG_UNBLOCK, &sigs, NULL);
#endif
}

SIGNAL_HANDLER(catch_segv)
{
    unblock_signal(SIGSEGV);

    handle_segv();
}
#endif /*defined __GNUC__ && __linux*/
}

namespace segvcatch
{
#ifdef __linux
void init_segv(handler h)
{
    if (h)
        handler_segv = h;
    else
        handler_segv = default_segv;
    INIT_SEGV;
}

void hook_errors()
{
    //init_segv(0);
}

#else
void hook_errors() {}
#endif

}

