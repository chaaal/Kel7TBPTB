const express = require("express");
const { Login,register,getUser,editProfile,changePassword } = require("../controllers/auth.js");
const {getRequests,getRequest,acceptRequest,rejectRequest,downloadRequest} = require("../controllers/request.js");
const {getInfos, getInfo, addInfo,getArchives,downloadProposal,downloadReply} = require("../controllers/Info.js");
const {getReplies,getReply,addReply,getUnreplies} = require("../controllers/reply.js");
const { isUserLoggedIn } = require("../middleware/isUserLoggedIn.js");
const upload = require('../config/multer.js');



const router = express.Router();

router.post('/login', Login);
router.get('/login', isUserLoggedIn, (req, res) => {
  res.render('login');
});

router.get('/user', (req, res) => {
  getUser(req, res);
});

router.post('/update-phone',editProfile)
router.post('/change-password',changePassword)

router.post('/register', register);

router.get('/requests', getRequests);
router.get('/requests/:id', getRequest);
router.post('/requests/:id/reject', rejectRequest);
router.post('/requests/:id/accept', acceptRequest);
router.get('/requests/:id/download', downloadRequest);


router.get('/infos', getInfos);
router.get('/infos/:id', getInfo);
router.post('/infos', addInfo);

router.get('/archives', getArchives);
router.get('/archives/:id/downloadProposal', downloadProposal);
router.get('/archives/:id/downloadReply', downloadReply);

router.get('/replies', getReplies);
router.get('/unreplies', getUnreplies);
router.get('/replies/:id', getReply);
router.get('/replies/:id/download', downloadReply);
router.post('/replies/:id', upload.single('reply'), addReply);



module.exports = router;
