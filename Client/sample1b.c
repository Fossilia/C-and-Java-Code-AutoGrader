#include <stdio.h>
#include <stdlib.h>

int main() {
	char buffer[500];
	int  n1, n2, n3;

	fgets(buffer, sizeof(buffer), stdin);
	n1 = atoi(buffer);
	
	fgets(buffer, sizeof(buffer), stdin);
	n2 = atoi(buffer);

	n3 = n1 + n2;
	printf("%d\n", n3);
}	
