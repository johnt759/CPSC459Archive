import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.HashMap;

/* Block Chain should maintain only limited block nodes to satisfy the functions
   You should not have the all the blocks added to the block chain in memory 
   as it would overflow memory
 */

public class BlockChain {
   public static final int CUT_OFF_AGE = 10;
   	private TransactionPool _txnPool = new TransactionPool();
	private BlockNode _blockChain;
	private BlockNode MaxHeightBlock;
   // all information required in handling a block in block chain
   private class BlockNode {
      public Block b;
      public BlockNode parent;
      public ArrayList<BlockNode> children;
      public int height;
      // utxo pool for making a new block on top of this block
      private UTXOPool uPool;

      public BlockNode(Block b, BlockNode parent, UTXOPool uPool) {
         this.b = b;
         this.parent = parent;
         children = new ArrayList<BlockNode>();
         this.uPool = uPool;
         if (parent != null) {
            height = parent.height + 1;
            parent.children.add(this);
         } else {
            height = 1;
         }
      }

      public UTXOPool getUTXOPoolCopy() {
         return new UTXOPool(uPool);
      }
   }
   
   private HashMap<ByteArrayWrapper, BlockNode> chains;

   /* create an empty block chain with just a genesis block.
    * Assume genesis block is a valid block
    */
   	public BlockChain(Block genesisBlock) {
   		chains = new HashMap<>();
		UTXOPool pool = new UTXOPool(); // upool
		TransactionPool tranpool = new TransactionPool(); //txnpool
		Transaction cointran = genesisBlock.getCoinbase(); //coinbasetxt
		tranpool.addTransaction(cointran);
		for (int i = 0; i < cointran.numOutputs(); i++) {
			byte[] getcoinhash = cointran.getHash();
			UTXO new_utxo = new UTXO(getcoinhash, i);
			Transaction.Output tranout = cointran.getOutput(i);
			pool.addUTXO(new_utxo, tranout);
		}
		ArrayList<Transaction> txns = genesisBlock.getTransactions();
      for(int i =0; i< txns.size();i++){
         if(txns.get(i)==null){
				int numOpOfCurrTX = txns.get(i).numOutputs();
				for (int j = 0; j < numOpOfCurrTX; j++) {
					byte[] txHash = txns.get(i).getHash();
					UTXO utxo = new UTXO(txHash, j);
					Transaction.Output txOut = txns.get(i).getOutput(j);
					pool.addUTXO(utxo, txOut);
				}
				tranpool.addTransaction(txns.get(i));
         }
      }
		
		_blockChain = new BlockNode(genesisBlock, null, pool);
		chains.put(new ByteArrayWrapper(genesisBlock.getHash()), _blockChain);
		MaxHeightBlock = _blockChain;
	}

   /* Get the maximum height block
    */
   public Block getMaxHeightBlock() {
      return MaxHeightBlock.b;
   }
   
   /* Get the UTXOPool for mining a new block on top of 
    * max height block
    */
   public UTXOPool getMaxHeightUTXOPool() {
      return  MaxHeightBlock.getUTXOPoolCopy();
   }
   
   /* Get the transaction pool to mine a new block
    */
   public TransactionPool getTransactionPool() {
      	return _txnPool;
   }

   /* Add a block to block chain if it is valid.
    * For validity, all transactions should be valid
    * and block should be at height > (maxHeight - CUT_OFF_AGE).
    * For example, you can try creating a new block over genesis block 
    * (block height 2) if blockChain height is <= CUT_OFF_AGE + 1. 
    * As soon as height > CUT_OFF_AGE + 1, you cannot create a new block at height 2.
    * Return true of block is successfully added
    */
   public boolean addBlock(Block b) {
	   if (b == null) // Return false if an empty block is found.
	   {
		   return false;
	   }
	   
	   byte[] previousHash = b.getPrevBlockHash();
	   
	   if (previousHash == null)
	   {
		   // Return false if the previous block hash is empty.
		   return false;
	   }
	   
	   BlockNode parentNode = chains.get(new ByteArrayWrapper(previousHash));
	   
	   if (parentNode == null)
	   {
		   return false;
	   }
	   
	   TxHandler handler = new TxHandler(parentNode.getUTXOPoolCopy());
	   
	   // Add the new transaction into the array.
	   Transaction[] newTxs = b.getTransactions().toArray(new Transaction[0]);
	   
	   Transaction[] validTxs = handler.handleTxs(newTxs);
	   
	   if (newTxs.length != validTxs.length)
	   {
		   return false; // Return false if the transaction lengths aren't equal.
	   }
	   
	   int newHeight = parentNode.height + 1;
	   if (newHeight <= MaxHeightBlock.height - CUT_OFF_AGE)
	   {
		   return false; // If the block is too short, then return false.
	   }
	   UTXOPool utxopool = handler.getUTXOPool();
	   Transaction coinbase = b.getCoinbase();
	   for (int i = 0; i < coinbase.numOutputs(); i++)
	   {
		   Transaction.Output output = coinbase.getOutput(i);
		   UTXO newutxo = new UTXO(coinbase.getHash(), i);
		   utxopool.addUTXO(newutxo,  output);
	   }
	   BlockNode newNode = new BlockNode(b, parentNode, utxopool);
	   chains.put(new ByteArrayWrapper(b.getHash()), newNode);
	   if (newHeight > MaxHeightBlock.height)
	   {
		   MaxHeightBlock = newNode;
	   }
	   
       return true;
   }

   /* Add a transaction in transaction pool
    */
   public void addTransaction(Transaction tx) {
      this._txnPool.addTransaction(tx);
   }
}
