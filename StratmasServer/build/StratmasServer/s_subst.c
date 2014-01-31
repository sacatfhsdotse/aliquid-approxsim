#include <stdio.h>
#include <stdlib.h>

/*
 * Filters stdin and substitutes any occurances of argv[1] for
 * argv[2], if no argv[2], removes any occurances of argv[1].
 * Currently buggy, use with care.
 */

char* progname;

int main(int argc, char** argv)
{
  char* replacee = NULL;
  char* replacement = NULL;
  int c, i, m;

  if (argc > 0) {
    progname = argv[0];
  } else {
    progname = "subst";
  }

  if (argc < 2 || argc > 3) {
    fprintf(stderr, "Usage: %s string-to-replace [replacer]\n", progname);
    exit(1);
  } else if (argc == 2) {
    replacee = argv[1];
  } else { // argc == 3;
    replacee = argv[1];
    replacement = argv[2];
  }

  m = 0;
  while ((c = fgetc(stdin)) != EOF) {
    if ((char) c == replacee[m]) {
      // Prefix match, if at end of replacee, output replacement (if
      // any), else look at next input.
      if (replacee[m + 1] == '\0') {
	if (replacement != NULL) {
	  if (fputs(replacement, stdout) == EOF) {
	    fprintf(stderr, "%s: error writing to stdout\n", 
		    progname);
	    exit(1);
	  }
	}
	m = 0;
      } else {
	m++;
      }
    } else {
      // Missmatch.
      if (m == 0) {
	if (putc(c, stdout) == EOF) {
	  fprintf(stderr, "%s: error writing to stdout\n", 
		  progname);
	  exit(1);
	}
      } else {
	// If there is currently a matching prefix, it has to
	// be investigated for suffixes that are prefixes of itself.  A
	// Perhaps more efficient way would be to pre-investigate
	// replacee for potential suffixes == prefix, however this
	// program is supposed to be simple...
	for (i = 1; i < m; i++) {
	  if ((char) c == replacee[m - i + 1] &&
	      strncmp(replacee, &replacee[i], m - i) == 0) {
	    fprintf(stdout, "%d:%d\n", m, i);
	    // New partial match
	    break;
	  }
	}
	// Write remainder
	if (fwrite(replacee, sizeof(char), i, stdout) != i) {
	  fprintf(stderr, "%s: error writing to stdout\n", 
		  progname);
	  exit(1);
	}
	m = m - i; 

	if (m == 0) {
	  if (putc(c, stdout) == EOF) {
	    fprintf(stderr, "%s: error writing to stdout\n", 
		    progname);
	    exit(1);
	  }
	}
      }
    }
  }

  // If we are collecting a partial match at EOF, write partial match.
  if (fwrite(replacee, sizeof(char), m, stdout) != m) {
    fprintf(stderr, "%s: error writing to stdout\n", 
	    progname);
    exit(1);
  }

  // Make sure everything gets written.
  if (fflush(stdout) == EOF) {
    fprintf(stderr, "%s: error writing to stdout\n", 
	    progname);
    exit(1);
  }

  return 0;
}
