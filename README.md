# Fix Client Example

This project implements the FIX protocol(Financial Information eXchange).
It creates a Logon and at the moment manages a subscription of a symbol with the message _MarketDataRequest_
and process the responses of it (_MarketDataSnapshotFullRefresh_ and _MarketDataIncrementalRefresh_)

This library support the protocol version 5.0 and the transport protocol version 1.1