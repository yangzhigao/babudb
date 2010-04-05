// This file is part of babudb/cpp
//
// Copyright (c) 2008, Felix Hupfeld, Jan Stender, Bjoern Kolbeck, Mikael Hoegqvist, Zuse Institute Berlin.
// Copyright (c) 2009, Felix Hupfeld
// Licensed under the BSD License, see LICENSE file for details.
//
// Author: Felix Hupfeld (felix@storagebox.org)

// The Log bundles a series of LogSections with contiguous LSNs

#ifndef LOG__LOG_H
#define LOG__LOG_H

#include <string>
using std::string;
#include <vector>
#include <map>

#include "babudb/log/log_section.h"
#include "babudb/log/log_iterator.h"

namespace babudb {

class MergedIndex;
class OperationFactory;
class MergedIndexOperationTarget;
template <class T> class LogIterator;

class Log {
public:
  Log(Buffer data);  // a volatile in-memory log
	Log(const string& name_prefix);
	~Log();

	void loadRequiredLogSections(lsn_t);
	void close();

	void cleanup(lsn_t from_lsn, const string& to);

	LogSection* getTail();
	void advanceTail();
	lsn_t getLastLSN();

	typedef LogIteratorForward iterator;
	typedef LogIteratorBackward reverse_iterator;

	iterator begin();
	iterator end();
	reverse_iterator rbegin();
	reverse_iterator rend();

	std::vector<LogSection*>& getSections() { return sections; }

private:
	std::vector<LogSection*> sections;
	LogSection* tail;
	string name_prefix;
};

};

#endif